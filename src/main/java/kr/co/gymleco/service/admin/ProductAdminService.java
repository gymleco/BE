package kr.co.gymleco.service.admin;

import kr.co.gymleco.admin.ProductAdminResponse;
import kr.co.gymleco.admin.ProductCreateRequest;
import kr.co.gymleco.admin.ProductRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.domain.product.*;
import kr.co.gymleco.infra.revalidate.ContentChangedEvent;
import kr.co.gymleco.support.html.HtmlSanitizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductAdminService {

    private final ProductRepository repository;
    private final HtmlSanitizer sanitizer;
    private final AuditLogger auditLogger;
    private final ApplicationEventPublisher events;

    public ProductAdminService(ProductRepository repository,
                               HtmlSanitizer sanitizer,
                               AuditLogger auditLogger,
                               ApplicationEventPublisher events) {
        this.repository = repository;
        this.sanitizer = sanitizer;
        this.auditLogger = auditLogger;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<ProductAdminResponse> list(ProductType type) {
        // 정렬과 필터를 DB 로 내린다. 메모리에서 거르지 않는다.
        List<Product> products = (type == null)
                ? repository.findAllByOrderBySortOrderAscIdAsc()
                : repository.findByTypeOrderBySortOrderAscIdAsc(type);
        return products.stream().map(ProductAdminResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductAdminResponse detail(Long id) {
        return ProductAdminResponse.from(find(id));
    }

    /* ═══════════════════ 등록 ═══════════════════ */

    @Transactional
    public Long create(ProductCreateRequest request, AdminUser actor, String ip) {
        if (repository.existsBySlug(request.slug())) {
            throw new DuplicateSlugException(request.slug());
        }

        ProductRequest body = request.product();
        Product product = switch (body.type()) {
            case EQUIPMENT -> Product.ofEquipment(
                request.slug(), body.category(), body.nameKo(), body.nameEn());
            case PART -> Product.ofPart(request.slug(), body.nameKo(), body.nameEn());
            case ACCESSORY -> Product.ofAccessory(request.slug(), body.nameKo(), body.nameEn());
        };

        applyBody(product, body);
        Product saved = repository.save(product);

        auditLogger.contentChanged(actor, AuditAction.PRODUCT_CREATED,
            "Product", saved.getId(), saved.getSlug(), ip);

        /*
         * 새 제품은 비공개로 시작하므로 공개 사이트에 나타나지 않는다.
         * 재검증을 부를 필요가 없다 — 불필요한 호출은 캐시만 흔든다.
         */
        return saved.getId();
    }

    /* ═══════════════════ 수정 ═══════════════════ */

    @Transactional
    public void update(Long id, ProductRequest body, AdminUser actor, String ip) {
        Product product = find(id);

        // slug 는 바꾸지 않는다. type 도 바꾸지 않는다.
        if (product.getType() != body.type()) {
            throw new IllegalArgumentException(
                "품목 종류는 변경할 수 없습니다. 새로 등록하고 기존 항목은 비공개로 두세요.");
        }

        product.changeCategory(body.category());
        applyBody(product, body);

        auditLogger.contentChanged(actor, AuditAction.PRODUCT_UPDATED,
            "Product", id, product.getSlug(), ip);

        // 공개 중인 제품만 사이트에 영향을 준다
        if (product.isVisible()) {
            events.publishEvent(ContentChangedEvent.product(product.getSlug()));
        }
    }

    /* ═══════════════════ 공개 여부 ═══════════════════ */

    @Transactional
    public void changeVisibility(Long id, boolean visible, AdminUser actor, String ip) {
        Product product = find(id);
        product.changeVisibility(visible);

        auditLogger.contentChanged(actor, AuditAction.PRODUCT_VISIBILITY_CHANGED,
            "Product", id, visible ? "공개" : "비공개", ip);

        /*
         * 공개 → 비공개도 재검증해야 한다.
         * 그러지 않으면 내린 제품이 캐시에 남아 계속 노출된다.
         */
        events.publishEvent(ContentChangedEvent.product(product.getSlug()));
    }

    /* ═══════════════════ 순서 ═══════════════════ */

    /**
     * 목록 순서 일괄 변경.
     *
     * 한 건씩 PATCH 하면 드래그 한 번에 요청이 수십 개 날아가고,
     * 중간에 실패하면 순서가 뒤엉킨다. 전체를 한 트랜잭션으로 받는다.
     */
    @Transactional
    public void reorder(Map<Long, Integer> orders, AdminUser actor, String ip) {
        List<Product> products = repository.findAllById(orders.keySet());
        if (products.size() != orders.size()) {
            throw new ProductNotFoundException(null);
        }
        products.forEach(p -> p.changeSortOrder(orders.get(p.getId())));

        auditLogger.contentChanged(actor, AuditAction.PRODUCT_UPDATED,
            "Product", null, "정렬 순서 " + orders.size() + "건 변경", ip);

        events.publishEvent(ContentChangedEvent.settings());
    }

    /* ═══════════════════ 삭제 ═══════════════════ */

    /**
     * ★ 삭제는 신중하게.
     *
     * 문의(inquiry_product)와 중고(used_item)가 제품을 참조한다.
     * 지우면 "어떤 제품에 대한 문의였는지" 를 영영 알 수 없게 된다.
     * 관리 화면은 비공개 전환을 기본으로 안내하고, 삭제는 확인창을 거친다.
     */
    @Transactional
    public void delete(Long id, AdminUser actor, String ip) {
        Product product = find(id);
        String slug = product.getSlug();

        repository.delete(product);

        auditLogger.contentChanged(actor, AuditAction.PRODUCT_DELETED,
            "Product", id, slug, ip);
        events.publishEvent(ContentChangedEvent.product(slug));
    }

    /* ═══════════════════ 내부 ═══════════════════ */

    private void applyBody(Product product, ProductRequest body) {
        /*
         * ★ 살균은 여기 한 곳에서만 한다.
         *
         * 출력 시점에 거르면 거르는 곳을 하나라도 빠뜨리는 순간 뚫린다.
         * 저장 전에 한 번 걸러 두면 DB 에 위험한 값이 애초에 없다.
         *
         * summary 는 서식이 필요 없으므로 태그를 전부 제거한다 —
         * 목록 카드와 메타 설명에 그대로 들어가는 값이다.
         */
        product.editContent(
            body.nameKo(),
            body.nameEn(),
            sanitizer.stripToText(body.summary()),
            sanitizer.sanitize(body.description()));

        product.applyDimensions(
            body.footprintM2(), body.widthMm(), body.depthMm(),
            body.heightMm(), body.weightKg());

        product.changeThumbnail(body.thumbnailKey());
        product.changeCutout(body.cutoutKey());
    }

    private Product find(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
