package kr.co.gymleco.domain.center;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "official_center")
public class OfficailCenter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140, updatable = false)
    private String slug;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(nullable = false, length = 40)
    private String region;
    @Column(nullable = false, length = 255)
    private String address = "";
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
    @Column(nullable = false, length = 40)
    private String phone = "";
    @Column(name = "website_url", nullable = false, length = 255)
    private String websiteUrl = "";
    @Column(name = "instagram_url", nullable = false, length = 255)
    private String instagramUrl = "";
    @Column(nullable = false, length = 200)
    private String summary = "";
    @Column(nullable = false, columnDefinition = "text")
    private String description = "";
    @Column(name = "area_pyeong")
    private Short areaPyeong;
    @Column(name = "opened_at")
    private LocalDate openedAt;
    @Column(name = "thumbnail_key", length =255)
    private String thumbnailKey;
    @Column(name = "consent_at")
    private Instant consentAt;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    @Column(nullable = false)
    private boolean visible = false;
    @Column(name  ="created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected OfficialCenter() {}
    public static OfficialCenter of(String slug, String name, String region) {
        OfficialCenter c = new OfficialCenter();
        c.slug = slug;
        c.name = name;
        c.region = region;
        return c;
    }
}
