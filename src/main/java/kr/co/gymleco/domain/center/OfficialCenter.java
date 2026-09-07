package kr.co.gymleco.domain.center;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "official_center")
public class OfficialCenter {

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
    @Column(name = "thumbnail_key", length = 255)
    private String thumbnailKey;
    @Column(name = "consent_at")
    private Instant consentAt;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    @Column(nullable = false)
    private boolean visible = false;
    @Column(name = "created_at", nullable = false, updatable = false)
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
    public void edit(String name, String region, String address, String phone, String websiteUrl, String instagramUrl, String summary, String sanitizedDescription, Short areaPyeong, LocalDate openedAt, String thumbnailKey) {
        this.name = name;
        this.region = region;
        this.address = nz(address);
        this.phone = nz(phone);
        this.websiteUrl = nz(websiteUrl);
        this.instagramUrl = nz(instagramUrl);
        this.summary = nz(summary);
        this.description = nz(sanitizedDescription);
        this.areaPyeong = areaPyeong;
        this.openedAt = openedAt;
        this.thumbnailKey = thumbnailKey;
        touch();
    }
    public void applyCoords(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("위도와 경도는 둘 다 넣거나 둘 다 비워야 합니다.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        touch();
    }
    public void recordConsent(Instant at) {
        this.consentAt = at;
        touch();
    }
    public void changeVisibility(boolean visible) {
        if (visible && consentAt == null) {
            throw new IllegalArgumentException(
                "게재 동의를 받은 날짜가 있어야 공개할 수 있습니다.");
        }
        this.visible = visible;
        touch();
    }
    public void changeSortOrder(int sortOrder) { this.sortOrder = sortOrder; touch(); }
    private void touch() { this.updatedAt = Instant.now(); }
    private static String nz(String v) { return v == null ? "" : v; }
    public Long getId()               { return id; }
    public String getSlug()           { return slug; }
    public String getName()           { return name; }
    public String getRegion()         { return region; }
    public String getAddress()        { return address; }
    public BigDecimal getLatitude()   { return latitude; }
    public BigDecimal getLongitude()  { return longitude; }
    public String getPhone()          { return phone; }
    public String getWebsiteUrl()     { return websiteUrl; }
    public String getInstagramUrl()   { return instagramUrl; }
    public String getSummary()        { return summary; }
    public String getDescription()    { return description; }
    public Short getAreaPyeong()      { return areaPyeong; }
    public LocalDate getOpenedAt()    { return openedAt; }
    public String getThumbnailKey()   { return thumbnailKey; }
    public Instant getConsentAt()     { return consentAt; }
    public int getSortOrder()         { return sortOrder; }
    public boolean isVisible()        { return visible; }
    public Instant getUpdatedAt()     { return updatedAt; }
}
