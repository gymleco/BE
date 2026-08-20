package kr.co.gymleco.service.admin;

import kr.co.gymleco.admin.InquiryDetail;
import kr.co.gymleco.admin.InquiryListItem;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.inquiry.Inquiry;
import kr.co.gymleco.domain.inquiry.InquiryRepository;
import kr.co.gymleco.domain.inquiry.InquiryStatus;
import kr.co.gymleco.security.crypto.PiiEncryptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.co.gymleco.support.csv.CsvWriter;
import org.springframework.data.domain.PageRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InquiryAdminService {

    private final InquiryRepository repository;
    private final PiiEncryptor piiEncryptor;
    private final AuditLogger auditLogger;

    public InquiryAdminService(InquiryRepository repository,
                               PiiEncryptor piiEncryptor,
                               AuditLogger auditLogger) {
        this.repository = repository;
        this.piiEncryptor = piiEncryptor;
        this.auditLogger = auditLogger;
    }

    @Transactional(readOnly = true)
    public Page<InquiryListItem> list(InquiryStatus status, Pageable pageable) {
        Page<Inquiry> page = (status == null)
            ? repository.findAllByOrderByCreatedAtDesc(pageable)
            : repository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return page.map(InquiryListItem::from);
    }
    @Transactional(readOnly = true)
    public InquiryDetail detail(Long id, AdminUser viewer, String ip) {
        Inquiry inquiry = repository.findById(id)
            .orElseThrow(() -> new InquiryNotFoundException(id));

        auditLogger.inquiryViewed(viewer, id, ip);

        return InquiryDetail.of(inquiry, piiEncryptor.decrypt(inquiry.getPhoneEncrypted()));
    }

    @Transactional
    public void changeStatus(Long id, InquiryStatus next, AdminUser actor, String ip) {
        Inquiry inquiry = repository.findById(id)
            .orElseThrow(() -> new InquiryNotFoundException(id));

        String before = inquiry.getStatus().name();
        inquiry.changeStatus(next);

        auditLogger.inquiryStatusChanged(actor, id, before, next.name(), ip);
    }

    @Transactional
    public void writeMemo(Long id, String memo, AdminUser actor, String ip) {
        Inquiry inquiry = repository.findById(id)
            .orElseThrow(() -> new InquiryNotFoundException(id));
        inquiry.writeMemo(memo);
        auditLogger.contentChanged(actor,
            kr.co.gymleco.domain.audit.AuditAction.INQUIRY_STATUS_CHANGED,
            "Inquiry", id, "메모 수정", ip);
    }
    /*
     * ── 내보내기 방침 ──────────────────────────────────────
     *
     * 이 메서드는 컬럼 암호화를 무력화하는 유일한 통로다.
     * 전화번호를 전부 복호화해 평문 파일로 만들어 관리자 PC 로 내려보낸다.
     * DB 를 아무리 잘 지켜도 이 파일이 카톡으로 돌아다니면 끝이다.
     * 그래서 세 가지를 강제한다.
     *
     *   1. 기간이 필수다 (컨트롤러에서 required)
     *      "전체 내보내기" 버튼이 없으면 전체가 새어나갈 수 없다.
     *   2. 5000건 상한
     *      메모리 보호가 아니라 유출량 상한이다.
     *   3. 몇 건을 어떤 조건으로 뽑았는지 감사 로그에 남긴다
     */
    private static final int MAX_EXPORT_ROWS = 5_000;
    private static final int CHUNK = 500;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TS =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(KST);
    private static final DateTimeFormatter FILE_TS =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").withZone(KST);

    public record CsvExport(byte[] content, String filename, int count, boolean truncated) {}

    @Transactional(readOnly = true)
    public CsvExport exportCsv(InquiryStatus status, LocalDate fromDate, LocalDate toDate,
                               AdminUser actor, String ip) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("시작일이 종료일보다 뒤입니다.");
        }
        Instant from = fromDate.atStartOfDay(KST).toInstant();
        Instant to = toDate.plusDays(1).atStartOfDay(KST).toInstant();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int count = 0;
        boolean truncated = false;
        try (CsvWriter csv = new CsvWriter(
            new OutputStreamWriter(buffer, StandardCharsets.UTF_8))) {
            csv.writeHeader(List.of(
                "번호", "접수일시", "유형", "이름", "연락처", "이메일", "회사", "지역", "공간 정보", "문의 내용", "상태", "메모", "동의일시", "마케팅 동의", "파기 예정일"));
            int pageNo = 0;
            Page<Inquiry> page;
            do {
                Pageable pageable = PageRequest.of(pageNo, CHUNK);
                page = (status == null)
                    ? repository.findInRange(from, to, pageable)
                    : repository.findInRangeByStatus(status, from, to, pageable);
                for (Inquiry inquiry : page.getContent()) {
                    if (count >= MAX_EXPORT_ROWS) {
                        truncated = true;
                        break;
                    }
                    csv.writeRow(toRow(inquiry));
                    count++;
                }
                pageNo++;
            } while (page.hasNext() && !truncated);

        } catch (IOException e) {
            throw new IllegalStateException("CSV 생성에 실패했습니다.", e);
        }
        String filter = "%s ~ %s%s".formatted(
            fromDate, toDate,
            status == null ? "" : " · 상태=" + status.name())
            + (truncated ? " · 상한 초과로 잘림" : "");
        auditLogger.inquiryExported(actor, count, filter, ip);
        return new CsvExport(buffer.toByteArray(), "inquiries-" + FILE_TS.format(Instant.now()) + ".csv", count, truncated);
    }
    private List<String> toRow(Inquiry inquiry) {
        List<String> row = new ArrayList<>(15);
        row.add(String.valueOf(inquiry.getId()));
        row.add(TS.format(inquiry.getCreatedAt()));
        row.add(inquiry.getType().label());
        row.add(inquiry.getName());
        row.add(piiEncryptor.decrypt(inquiry.getPhoneEncrypted()));
        row.add(inquiry.getEmail());
        row.add(inquiry.getCompany());
        row.add(inquiry.getRegion());
        row.add(inquiry.getSpaceInfo());
        row.add(inquiry.getMessage());
        row.add(inquiry.getStatus().label());
        row.add(inquiry.getMemo());
        row.add(TS.format(inquiry.getConsentAt()));
        row.add(inquiry.getMarketingConsentAt() == null
            ? "미동의" : TS.format(inquiry.getMarketingConsentAt()));
        row.add(inquiry.getPurgeAt() == null ? "" : TS.format(inquiry.getPurgeAt()));
        return row;
    }
}
