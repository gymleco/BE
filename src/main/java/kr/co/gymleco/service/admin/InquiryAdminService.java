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
}
