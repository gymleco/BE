package kr.co.gymleco.domain.product;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 120)
    private String slug;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;
    @Column(name = "name_ko", nullable = false, length = 120)
    private String nameKo;
    @Column(name = "name_en", nullable = false, length = 120)
    private String nameEn;
    @Column(nullable = false, length = 200)
    private String summary = "";
    @Column(nullable = false, columnDefinition = "text")
    private String description = "";
    @Column(name = "footprint_m2", precision = 6, scale = 2)
    private BigDecimal footprintM2;
    @Column(name = "width_mm")  private Integer widthMm;
    @Column(name = "depth_mm")  private Integer depthMm;
    @Column(name = "height_mm") private Integer heightMm;
    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;
    @Column(name = "thumbnail_key", length = 255)
    private String thumbnailKey;
    @Column(name = "cutout_key", length = 255)
    private String cutoutKey;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    @Column(nullable = false)
    private boolean visible = false;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected Product() {
    }
    private Product(String slug, ProductType type, ProductCategory category,
                    String nameKo, String nameEn) {
        this.slug = slug;
        this.type = type;
        this.category = category;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
    }
    public static Product ofEquipment(String slug, ProductCategory category, String nameKo, String nameEn) {
        return new Product(slug, ProductType.EQUIPMENT, category, nameKo, nameEn);
    }
    public static Product ofPart(String slug, String nameKo, String nameEn) {
        return new Product(slug, ProductType.PART, ProductCategory.PART, nameKo, nameEn);
    }
    public static Product ofAccessory(String slug, String nameKo, String nameEn) {
        return new Product(slug, ProductType.ACCESSORY, ProductCategory.ACCESSORY, nameKo, nameEn);
    }
    public void applyDimensions(BigDecimal footprintM2, Integer widthMm,
                                Integer depthMm, Integer heightMm, BigDecimal weightKg) {
        if (type.requiresFootprint() && footprintM2 == null) {
            throw new IllegalArgumentException("기구는 설치 면적이 필요합니다.");
        }
        this.footprintM2 = footprintM2;
        this.widthMm = widthMm;
        this.depthMm = depthMm;
        this.heightMm = heightMm;
        this.weightKg = weightKg;
        touch();
    }
    public void editContent(String nameKo, String nameEn, String summary,
                            String sanitizedDescription) {
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.summary = summary;
        this.description = sanitizedDescription;
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
    public void addImage(ProductImage image) {
        images.add(image);
        image.assignTo(this);
        touch();
    }
    public void changeCutout(String cutoutKey){
        this.cutoutKey = cutoutKey;
        touch();
    }
    /** 대표 이미지. 업로드 파이프라인이 만든 스토리지 키를 받는다. */
    public void changeThumbnail(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
        touch();
    }

    /**
     * 카테고리 변경.
     *
     * type 은 바꾸지 않는다 — 기구를 부품으로 바꾸면 치수 제약이
     * 뒤집히고, 이미 걸린 문의·중고 연결의 의미가 달라진다.
     * 종류를 잘못 만들었으면 새로 등록하고 기존 것은 비공개로 둔다.
     */
    public void changeCategory(ProductCategory category) {
        this.category = category;
        touch();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
    private void touch() {
        this.updatedAt = Instant.now();
    }
    public Long getId()                 { return id; }
    public String getSlug()             { return slug; }
    public ProductType getType()        { return type; }
    public ProductCategory getCategory(){ return category; }
    public String getNameKo()           { return nameKo; }
    public String getNameEn()           { return nameEn; }
    public String getSummary()          { return summary; }
    public String getDescription()      { return description; }
    public String getCutoutKey()        { return cutoutKey; }
    public BigDecimal getFootprintM2()  { return footprintM2; }
    public Integer getWidthMm()         { return widthMm; }
    public Integer getDepthMm()         { return depthMm; }
    public Integer getHeightMm()        { return heightMm; }
    public BigDecimal getWeightKg()     { return weightKg; }
    public String getThumbnailKey()     { return thumbnailKey; }
    public int getSortOrder()           { return sortOrder; }
    public boolean isVisible()          { return visible; }
    public Instant getUpdatedAt()       { return updatedAt; }
    public Instant getCreatedAt()       { return createdAt; }
    public List<ProductImage> getImages() { return List.copyOf(images); }
}
