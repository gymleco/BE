package kr.co.gymleco.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "product_image")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;
    @Column(name = "alt_text", nullable = false, length = 200)
    private String altText = "";
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected ProductImage() {
    }
    public ProductImage(String imageKey, String altText, int sortOrder) {
        this.imageKey = imageKey;
        this.altText = altText == null ? "" : altText;
        this.sortOrder = sortOrder;
    }
    void assignTo(Product product) {
        this.product = product;
    }
    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    public Long getId()          { return id; }
    public String getImageKey()  { return imageKey; }
    public String getAltText()   { return altText; }
    public int getSortOrder()    { return sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
}
