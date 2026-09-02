package kr.co.gymleco.service.admin;

import kr.co.gymleco.admin.FaqRequest;
import kr.co.gymleco.admin.NoticeRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.domain.support.*;
import kr.co.gymleco.infra.revalidate.ContentChangedEvent;
import kr.co.gymleco.support.html.HtmlSanitizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** FAQ 와 공지는 성격이 같아 한 서비스에서 다룬다 — 둘 다 고객센터의 글이다. */
@Service
public class SupportAdminService {

    private final FaqRepository faqs;
    private final NoticeRepository notices;
    private final HtmlSanitizer sanitizer;
    private final AuditLogger auditLogger;
    private final ApplicationEventPublisher events;

    public SupportAdminService(FaqRepository faqs,
                               NoticeRepository notices,
                               HtmlSanitizer sanitizer,
                               AuditLogger auditLogger,
                               ApplicationEventPublisher events) {
        this.faqs = faqs;
        this.notices = notices;
        this.sanitizer = sanitizer;
        this.auditLogger = auditLogger;
        this.events = events;
    }

    /* ── FAQ ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Faq> listFaq() {
        return faqs.findAllByOrderByCategoryAscSortOrderAscIdAsc();
    }

    @Transactional
    public Long createFaq(FaqRequest body, AdminUser actor, String ip) {
        Faq f = Faq.of(body.category(), body.question());
        applyFaq(f, body);
        faqs.save(f);
        auditLogger.contentChanged(actor, AuditAction.FAQ_CREATED,
            "Faq", f.getId(), body.question(), ip);
        publish();
        return f.getId();
    }

    @Transactional
    public void updateFaq(Long id, FaqRequest body, AdminUser actor, String ip) {
        Faq f = faqs.findById(id).orElseThrow(() -> new SupportNotFoundException("FAQ", id));
        applyFaq(f, body);
        auditLogger.contentChanged(actor, AuditAction.FAQ_UPDATED,
            "Faq", id, body.question(), ip);
        publish();
    }

    @Transactional
    public void deleteFaq(Long id, AdminUser actor, String ip) {
        Faq f = faqs.findById(id).orElseThrow(() -> new SupportNotFoundException("FAQ", id));
        String q = f.getQuestion();
        faqs.delete(f);
        auditLogger.contentChanged(actor, AuditAction.FAQ_DELETED, "Faq", id, q, ip);
        publish();
    }

    private void applyFaq(Faq f, FaqRequest body) {
        // 답변은 저장 시점에 살균한다. 출력 시점에 거르면 화면 하나를 빠뜨리는 순간 구멍이 된다.
        f.edit(body.category(), body.question(), sanitizer.sanitize(body.answer()));
        if (body.sortOrder() != null) f.changeSortOrder(body.sortOrder());
        if (body.visible() != null) f.changeVisibility(body.visible());
    }

    /* ── 공지 ──────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<Notice> listNotice() {
        return notices.findAllByOrderByPinnedDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Notice detailNotice(Long id) {
        return notices.findById(id).orElseThrow(() -> new SupportNotFoundException("공지", id));
    }

    @Transactional
    public Long createNotice(NoticeRequest body, AdminUser actor, String ip) {
        Notice n = Notice.of(body.title());
        applyNotice(n, body);
        notices.save(n);
        auditLogger.contentChanged(actor, AuditAction.NOTICE_CREATED,
            "Notice", n.getId(), body.title(), ip);
        publish();
        return n.getId();
    }

    @Transactional
    public void updateNotice(Long id, NoticeRequest body, AdminUser actor, String ip) {
        Notice n = notices.findById(id).orElseThrow(() -> new SupportNotFoundException("공지", id));
        applyNotice(n, body);
        auditLogger.contentChanged(actor, AuditAction.NOTICE_UPDATED,
            "Notice", id, body.title(), ip);
        publish();
    }

    @Transactional
    public void deleteNotice(Long id, AdminUser actor, String ip) {
        Notice n = notices.findById(id).orElseThrow(() -> new SupportNotFoundException("공지", id));
        String t = n.getTitle();
        notices.delete(n);
        auditLogger.contentChanged(actor, AuditAction.NOTICE_DELETED, "Notice", id, t, ip);
        publish();
    }

    private void applyNotice(Notice n, NoticeRequest body) {
        n.edit(body.title(), sanitizer.sanitize(body.body()),
            Boolean.TRUE.equals(body.pinned()));
        if (body.publishedAt() != null) n.changePublishedAt(body.publishedAt());
        if (body.visible() != null) n.changeVisibility(body.visible());
    }

    private void publish() {
        events.publishEvent(new ContentChangedEvent(List.of("/support/faq", "/support/notice")));
    }
}
