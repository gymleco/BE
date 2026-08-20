package kr.co.gymleco.domain.used;

import jakarta.persistence.*;
import kr.co.gymleco.domain.product.Product;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "used_item")
public class UsedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 140)
    private String slug;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    @Column(name = "name_ko", nullable = false, length = 140)
    private String nameKo;
    @Column(name = "model_name", nullable = false, length = 140)
    private String modelName = "";
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", nullable = false, length = 10)
    private UsedCondition conditionGrade;
    @Column(name = "year_made")
    private Short yearMade;
    @Column(name = "price_krw")
    private Integer priceKrw;
    @Column(nullable = false, columnDefinition = "text")
    private String description = "";
    @Column(name = "thumbnail_key", length = 255)
    private String thumbnailKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UsedStatus status = UsedStatus.AVAILABLE;
    @Column(nullable = false)
    private short quantity = 1;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    @Column(nullable = false)
    private boolean visible = false;
    @OneToMany(mappedBy = "usedItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<UsedItemImage> images = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected UsedItem() {
    }

    public static UsedItem of(String slug, String nameKo, UsedCondition grade) {
        UsedItem item = new UsedItem();
        item.slug = slug;
        item.nameKo = nameKo;
        item.conditionGrade = grade;
        return item;
    }

    public void describe(String modelName, Short yearMade, Integer priceKrw, String description) {
        if (priceKrw != null && priceKrw <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다. 협의 가격이면 비워 두세요.");
        }
        this.modelName = modelName == null ? "" : modelName;
        this.yearMade = yearMade;
        this.priceKrw = priceKrw;
        this.description = description == null ? "" : description;
        touch();
    }

    public void linkProduct(Product product) {
        this.product = product;
        // 연결하는 순간 모델명을 복사해 둔다 — 나중에 제품이 지워져도 남는다
        if (product != null && this.modelName.isBlank()) {
            this.modelName = product.getNameKo();
        }
        touch();
    }
    public void changeStatus(UsedStatus next) {
        this.status = next;
        if (next == UsedStatus.SOLD) {
            this.quantity = 0;
        }
        touch();
    }
    public void changeQuantity(short quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0보다 작을 수 없습니다.");
        }
        this.quantity = quantity;
        if (quantity == 0 && this.status == UsedStatus.AVAILABLE) {
            this.status = UsedStatus.SOLD;
        }
        touch();
    }
    public void changeThumbnail(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
        touch();
    }
    public void changeVisibility(boolean visible) {
        this.visible = visible;
        touch();
    }
    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        touch();
    }
    public void addImage(String imageKey, String altText, int sortOrder) {
        images.add(new UsedItemImage(this, imageKey, altText, sortOrder));
        touch();
    }
    public void clearImages() {
        images.clear();
        touch();
    }
    private void touch() {
        this.updatedAt = Instant.now();
    }
    public Long getId()                    { return id; }
    public String getSlug()                { return slug; }
    public Product getProduct()            { return product; }
    public String getNameKo()              { return nameKo; }
    public String getModelName()           { return modelName; }
    public UsedCondition getConditionGrade() { return conditionGrade; }
    public Short getYearMade()             { return yearMade; }
    public Integer getPriceKrw()           { return priceKrw; }
    public String getDescription()         { return description; }
    public String getThumbnailKey()        { return thumbnailKey; }
    public UsedStatus getStatus()          { return status; }
    public short getQuantity()             { return quantity; }
    public int getSortOrder()              { return sortOrder; }
    public boolean isVisible()             { return visible; }
    public List<UsedItemImage> getImages()  { return List.copyOf(images); }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getUpdatedAt()          { return updatedAt; }
}
