package kr.co.gymleco.service.admin;

import kr.co.gymleco.admin.BannerRequest;
import kr.co.gymleco.admin.SectionMediaRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.domain.banner.Banner;
import kr.co.gymleco.domain.banner.BannerPosition;
import kr.co.gymleco.domain.banner.BannerRepository;
import kr.co.gymleco.domain.banner.SectionMedia;
import kr.co.gymleco.domain.banner.SectionMediaRepository;
import kr.co.gymleco.infra.revalidate.ContentChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 배너와 섹션 이미지.
 *
 * 둘을 한 서비스에 둔 이유는 성격이 같기 때문이다 —
 * 화면에 깔리는 사진이고, PC·모바일 두 장을 함께 받고,
 * 바뀌면 해당 페이지를 다시 그려야 한다.
 */
@Service
public class BannerAdminService {

    private final BannerRepository banners;
    private final SectionMediaRepository sections;
    private final AuditLogger auditLogger;
    private final ApplicationEventPublisher events;

    public BannerAdminService(BannerRepository banners,
                              SectionMediaRepository sections,
                              AuditLogger auditLogger,
                              ApplicationEventPublisher events) {
        this.banners = banners;
        this.sections = sections;
        this.auditLogger = auditLogger;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<Banner> list() {
        return banners.findAllByOrderByPositionAscSortOrderAscIdAsc();
    }

    @Transactional
    public Long create(BannerRequest body, AdminUser actor, String ip) {
        Banner b = Banner.of(body.position(), body.imagePcKey(), body.imageMobileKey());
        apply(b, body);
        banners.save(b);

        auditLogger.contentChanged(actor, AuditAction.BANNER_CREATED,
            "Banner", b.getId(), body.position().label(), ip);
        publish(body.position());
        return b.getId();
    }

    @Transactional
    public void update(Long id, BannerRequest body, AdminUser actor, String ip) {
        Banner b = banners.findById(id)
            .orElseThrow(() -> new BannerNotFoundException(id));
        BannerPosition before = b.getPosition();

        b.changePosition(body.position());
        b.changeImages(body.imagePcKey(), body.imageMobileKey());
        apply(b, body);

        auditLogger.contentChanged(actor, AuditAction.BANNER_UPDATED,
            "Banner", id, body.position().label(), ip);

        /*
         * 위치를 옮겼으면 옛 페이지도 다시 그린다.
         * 안 그러면 배너가 사라진 페이지에 그대로 남아 있어
         * 두 곳에 걸린 것처럼 보인다.
         */
        publish(before);
        publish(body.position());
    }

    @Transactional
    public void delete(Long id, AdminUser actor, String ip) {
        Banner b = banners.findById(id)
            .orElseThrow(() -> new BannerNotFoundException(id));
        BannerPosition pos = b.getPosition();
        banners.delete(b);

        auditLogger.contentChanged(actor, AuditAction.BANNER_DELETED,
            "Banner", id, pos.label(), ip);
        publish(pos);
    }

    /* ── 섹션 이미지 ─────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<SectionMedia> listSections() {
        return sections.findAll();
    }

    /**
     * 등록과 수정을 나누지 않는다.
     * 구역 이름이 곧 기본키라, 있으면 덮고 없으면 만드는 것이 전부다.
     */
    @Transactional
    public void putSection(String key, SectionMediaRequest body,
                           AdminUser actor, String ip) {
        SectionMedia m = sections.findById(key).orElseGet(() -> SectionMedia.of(key));
        m.change(body.imagePcKey(), body.imageMobileKey(), body.altText());
        sections.save(m);

        auditLogger.contentChanged(actor, AuditAction.SECTION_MEDIA_UPDATED,
            "SectionMedia", null, key, ip);
        events.publishEvent(new ContentChangedEvent(List.of("/")));
    }

    /* ── 내부 ────────────────────────────────────────────── */

    private void apply(Banner b, BannerRequest body) {
        b.editText(body.title(), body.subtitle(), body.linkUrl());
        b.changePeriod(body.startsAt(), body.endsAt());
        if (body.sortOrder() != null) {
            b.changeSortOrder(body.sortOrder());
        }
        if (body.visible() != null) {
            b.changeVisibility(body.visible());
        }
    }

    /** 배너가 걸린 페이지만 다시 그린다 — 전체 재생성은 낭비다 */
    private void publish(BannerPosition position) {
        String path = switch (position) {
            case MAIN      -> "/";
            case PRODUCT   -> "/products";
            case USED      -> "/used";
            case PART      -> "/parts";
            case ACCESSORY -> "/accessories";
            case CENTER    -> "/centers";
        };
        events.publishEvent(new ContentChangedEvent(List.of(path)));
    }
}
