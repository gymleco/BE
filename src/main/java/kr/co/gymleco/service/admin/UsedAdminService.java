package kr.co.gymleco.service.admin;

import kr.co.gymleco.admin.UsedItemAdminResponse;
import kr.co.gymleco.admin.UsedItemCreateRequest;
import kr.co.gymleco.admin.UsedItemRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.domain.product.Product;
import kr.co.gymleco.domain.product.ProductRepository;
import kr.co.gymleco.domain.used.UsedItem;
import kr.co.gymleco.domain.used.UsedItemRepository;
import kr.co.gymleco.domain.used.UsedStatus;
import kr.co.gymleco.infra.revalidate.ContentChangedEvent;
import kr.co.gymleco.support.html.HtmlSanitizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsedAdminService {

    private final UsedItemRepository repository;
    private final ProductRepository products;
    private final HtmlSanitizer sanitizer;
    private final AuditLogger auditLogger;
    private final ApplicationEventPublisher events;

    public UsedAdminService(UsedItemRepository repository,
                            ProductRepository products,
                            HtmlSanitizer sanitizer,
                            AuditLogger auditLogger,
                            ApplicationEventPublisher events) {
        this.repository = repository;
        this.products = products;
        this.sanitizer = sanitizer;
        this.auditLogger = auditLogger;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<UsedItemAdminResponse> list(UsedStatus status) {
        List<UsedItem> found = (status == null)
            ? repository.findAllByOrderBySortOrderAscIdAsc()
            : repository.findByStatusOrderBySortOrderAscIdAsc(status);
        return found.stream().map(UsedItemAdminResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public UsedItemAdminResponse detail(Long id) {
        return UsedItemAdminResponse.from(repository.findWithImagesById(id).orElseThrow(() -> new UsedItemNotFoundException(id)));
    }
    @Transactional
    public Long create(UsedItemCreateRequest request, AdminUser actor, String ip) {
        if (repository.existsBySlug(request.slug())) {
            throw new DuplicateSlugException(request.slug());
        }
        UsedItemRequest body = request.item();

        UsedItem item = UsedItem.of(
            request.slug(), body.nameKo(), body.conditionGrade());
        apply(item, body);
        repository.save(item);

        auditLogger.contentChanged(actor, AuditAction.USED_CREATED,
            "UsedItem", item.getId(), body.nameKo(), ip);
        publish();
        return item.getId();
    }
    @Transactional
    public void update(Long id, UsedItemRequest body, AdminUser actor, String ip) {
        UsedItem item = repository.findById(id)
            .orElseThrow(() -> new UsedItemNotFoundException(id));
        apply(item, body);

        auditLogger.contentChanged(actor, AuditAction.USED_UPDATED,
            "UsedItem", id, body.nameKo(), ip);
        publish();
    }
    @Transactional
    public void changeStatus(Long id, UsedStatus next, AdminUser actor, String ip) {
        UsedItem item = repository.findById(id)
            .orElseThrow(() -> new UsedItemNotFoundException(id));
        String before = item.getStatus().name();
        item.changeStatus(next);

        auditLogger.contentChanged(actor, AuditAction.USED_STATUS_CHANGED,
            "UsedItem", id, before + " → " + next.name(), ip);
        publish();
    }
    @Transactional
    public void changeVisibility(Long id, boolean visible, AdminUser actor, String ip) {
        UsedItem item = repository.findById(id)
            .orElseThrow(() -> new UsedItemNotFoundException(id));
        item.changeVisibility(visible);

        auditLogger.contentChanged(actor, AuditAction.USED_VISIBILITY_CHANGED,
            "UsedItem", id, visible ? "공개" : "비공개", ip);
        publish();
    }
    @Transactional
    public void delete(Long id, AdminUser actor, String ip) {
        UsedItem item = repository.findById(id)
            .orElseThrow(() -> new UsedItemNotFoundException(id));
        String name = item.getNameKo();
        repository.delete(item);

        auditLogger.contentChanged(actor, AuditAction.USED_DELETED,
            "UsedItem", id, name, ip);
        publish();
    }
    private void apply(UsedItem item, UsedItemRequest body) {

        item.describe(
            body.modelName(),
            body.yearMade(),
            body.priceKrw(),
            sanitizer.sanitize(body.description()));

        if (body.thumbnailKey() != null) {
            item.changeThumbnail(body.thumbnailKey());
        }
        if (body.quantity() != null) {
            item.changeQuantity(body.quantity());
        }
        if (body.productId() != null) {
            Product p = products.findById(body.productId())
                .orElseThrow(() -> new ProductNotFoundException(body.productId()));
            item.linkProduct(p);
        }
    }
    private void publish() {
        events.publishEvent(new ContentChangedEvent(List.of("/used")));
    }
}
