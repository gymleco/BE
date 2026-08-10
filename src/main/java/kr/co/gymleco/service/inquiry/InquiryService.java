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

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final PiiEncryptor piiEncryptor;
    private final BlindIndexer blindIndexer;
    private final ApplicationEventPublisher eventPublisher;
    private final GymlecoProperties.Inquiry config;
    public InquiryService(InquiryRepository inquiryRepository,
                          PiiEncryptor piiEncryptor,
                          BlindIndexer blindIndexer,
                          ApplicationEventPublisher eventPublisher,
                          GymlecoProperties properties) {
        this.inquiryRepository = inquiryRepository;
        this.piiEncryptor = piiEncryptor;
        this.blindIndexer = blindIndexer;
        this.eventPublisher = eventPublisher;
        this.config = properties.inquiry();
    }
    @Transactional
    public Long receive(InquiryCommand command) {
        Instant now = Instant.now();
        if (command.sourceIp() != null && !command.sourceIp().isBlank()) {
            long recent = inquiryRepository.countRecentByIp(
                command.sourceIp(), now.minus(Duration.ofHours(1)));
            if (recent >= config.rateLimitPerHour()) {
                throw new RateLimitExceededException();
            }
        }
        String normalizedPhone = PhoneNumbers.normalize(command.phone());
        if (!PhoneNumbers.isPlausible(normalizedPhone)) {
            throw new IllegalArgumentException("연락처 형식을 확인해 주세요.");
        }

        String encryptedPhone = piiEncryptor.encrypt(normalizedPhone);
        byte[] blindIndex = blindIndexer.index(normalizedPhone);

        Inquiry inquiry = Inquiry.receive(
            command.type(),
            command.name().trim(),
            encryptedPhone,
            blindIndex,
            now,
            Duration.ofDays(config.retentionDays())
        );
        inquiry.applyOptionalFields(
            command.email(), command.company(), command.region(), command.message());
        if (command.marketingConsent()) {
            inquiry.applyMarketingConsent(now);
        }
        inquiry.applyRequestContext(command.sourceIp(), command.userAgent());

        if (command.productIds() != null && !command.productIds().isEmpty()) {
            inquiry.linkProducts(new HashSet<>(command.productIds()));
        }
        Inquiry saved = inquiryRepository.save(inquiry);
        eventPublisher.publishEvent(new InquiryReceivedEvent(
            saved.getId(), saved.getType(), saved.maskedName(),
            saved.getCompany(), saved.getRegion()));

        return saved.getId();
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
