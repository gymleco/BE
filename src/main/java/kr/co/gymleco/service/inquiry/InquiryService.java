package kr.co.gymleco.service.inquiry;

import kr.co.gymleco.config.GymlecoProperties;
import kr.co.gymleco.domain.inquiry.Inquiry;
import kr.co.gymleco.domain.inquiry.InquiryRepository;
import kr.co.gymleco.security.crypto.BlindIndexer;
import kr.co.gymleco.security.crypto.PhoneNumbers;
import kr.co.gymleco.security.crypto.PiiEncryptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.co.gymleco.domain.product.Product;
import kr.co.gymleco.domain.product.ProductRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import kr.co.gymleco.domain.inquiry.ReferenceNumbers;
import java.util.Optional;

@Service
public class InquiryService {
    private static final int REFERENCE_ATTEMPTS = 5;
    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final PiiEncryptor piiEncryptor;
    private final BlindIndexer blindIndexer;
    private final ApplicationEventPublisher eventPublisher;
    private final GymlecoProperties.Inquiry config;
    public InquiryService(InquiryRepository inquiryRepository,
                          ProductRepository productRepository,
                          PiiEncryptor piiEncryptor,
                          BlindIndexer blindIndexer,
                          ApplicationEventPublisher eventPublisher,
                          GymlecoProperties properties) {
        this.inquiryRepository = inquiryRepository;
        this.productRepository = productRepository;
        this.piiEncryptor = piiEncryptor;
        this.blindIndexer = blindIndexer;
        this.eventPublisher = eventPublisher;
        this.config = properties.inquiry();
    }
    @Transactional
    public InquiryReceipt receive(InquiryCommand command) {
        Instant now = Instant.now();
        if (command.sourceIp() != null && !command.sourceIp().isBlank()) {
            long recent = inquiryRepository.countRecentByIp(
                command.sourceIp(), now.minus(Duration.ofHours(1)));
            if (recent >= config.rateLimitPerHour()) {
                throw new RateLimitExceededException();
            }
        }
        /*
         * 동의는 컨트롤러의 @Valid 가 이미 걸렀지만 여기서 한 번 더 본다.
         * 이 검사가 없으면, 검증 없이 서비스를 직접 호출하는 경로가 생겼을 때
         * consent_at 에 "동의한 적 없는 시각"이 기록된다 — 법적 기록이
         * 거짓이 되는 버그라서 2겹으로 막는다.
         */
        if (!command.privacyConsent()) {
            throw new IllegalArgumentException("개인정보 수집·이용 동의가 필요합니다.");
        }
        String normalizedPhone = PhoneNumbers.normalize(command.phone());
        if (!PhoneNumbers.isPlausible(normalizedPhone)) {
            throw new IllegalArgumentException("연락처 형식을 확인해 주세요.");
        }

        String encryptedPhone = piiEncryptor.encrypt(normalizedPhone);
        byte[] blindIndex = blindIndexer.index(normalizedPhone);

        Inquiry inquiry = Inquiry.receive(
            command.type(),
            newReferenceNo(now),
            command.name().trim(),
            encryptedPhone,
            blindIndex,
            now,
            Duration.ofDays(config.retentionDays())
        );
        inquiry.applyOptionalFields(
            command.email(), command.company(), command.region(),
            command.spaceInfo(), command.message());
        if (command.marketingConsent()) {
            inquiry.applyMarketingConsent(now);
        }
        inquiry.applyRequestContext(command.sourceIp(), command.userAgent());

        /*
         * 모르는 slug 는 조용히 버린다 (400 이 아니라).
         * 관심 제품은 부가 정보일 뿐이고, 문의 본체가 가치다 —
         * 체크박스 하나 때문에 문의 전체를 반려하지 않는다.
         * 봇이 slug 목록을 탐색하는 용도로도 쓸 수 없게 된다.
         */
        if (command.productSlugs() != null && !command.productSlugs().isEmpty()) {
            List<Long> ids = productRepository
                .findBySlugInAndVisibleTrue(command.productSlugs())
                .stream()
                .map(Product::getId)
                .toList();
            if (!ids.isEmpty()) {
                inquiry.linkProducts(new HashSet<>(ids));
            }
        }

        /*
         * 재문의 묶기 — 같은 연락처로 최근에 들어온 문의가 있으면 그 건에 묶는다.
         *
         * ★ 버리지 않는다. 두 번째에 적은 내용(다른 제품, 바뀐 평수)이 사라진다.
         * ★ 앞 건의 번호를 돌려주지 않는다. 그러면 남의 전화번호를 넣어 보는 것만으로
         *   그 사람의 접수번호가 튀어나오는 «조회 통로» 가 된다.
         *   누구에게나 새 번호를 주고, 묶음은 관리 화면에서만 보인다.
         */
        Optional<Inquiry> earlier = inquiryRepository
            .findFirstByPhoneBlindIndexAndCreatedAtAfterOrderByCreatedAtDesc(
                blindIndex, now.minus(config.duplicateWindow()));
        earlier.ifPresent(inquiry::markDuplicateOf);

        Inquiry saved = inquiryRepository.save(inquiry);

        eventPublisher.publishEvent(new InquiryReceivedEvent(
            saved.getId(),
            saved.getReferenceNo(),
            earlier.map(this::rootReferenceNo).orElse(null),
            saved.getType(), saved.maskedName(),
            saved.getCompany(), saved.getRegion()));

        return new InquiryReceipt(saved.getReferenceNo());
    }

    /**
     * 겹치지 않는 접수번호를 뽑는다.
     * 하루에 92만 개 넘는 조합이라 겹칠 일은 드물지만, 드물다는 것과
     * 없다는 것은 다르다. 겹치면 다시 뽑는다.
     */
    private String newReferenceNo(Instant now) {
        for (int i = 0; i < REFERENCE_ATTEMPTS; i++) {
            String candidate = ReferenceNumbers.next(now);
            if (!inquiryRepository.existsByReferenceNo(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("접수번호를 만들지 못했습니다.");
    }

    /** 묶음의 원래 건 번호. 원래 건이 이미 파기됐으면 가까운 건의 번호로 대신한다. */
    private String rootReferenceNo(Inquiry earlier) {
        if (earlier.getDuplicateOfId() == null) {
            return earlier.getReferenceNo();
        }
        return inquiryRepository.findById(earlier.getDuplicateOfId())
            .map(Inquiry::getReferenceNo)
            .orElse(earlier.getReferenceNo());
    }
    @Transactional(readOnly = true)
    public List<Inquiry> searchByPhone(String rawPhone) {
        String normalized = PhoneNumbers.normalize(rawPhone);
        if (!PhoneNumbers.isPlausible(normalized)) {
            return List.of();
        }
        return inquiryRepository.findByPhoneBlindIndexOrderByCreatedAtDesc(
            blindIndexer.index(normalized));
    }
    @Transactional(readOnly = true)
    public String decryptPhone(Inquiry inquiry) {
        return piiEncryptor.decrypt(inquiry.getPhoneEncrypted());
    }
}
