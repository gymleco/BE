package kr.co.gymleco.domain.inquiry;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
@Entity
@Table(name = "inquiry")
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryType type;
    @Column(nullable = false, length = 80)
    private String name;
    /** AES-256-GCM 암호문(base64)*/
    @Column(name = "phone_encrypted", nullable = false, columnDefinition = "text")
    private String phoneEncrypted;
    /** HMAC-SHA256(정규화된 번호)*/
    @Column(name = "phone_blind_index")
    private byte[] phoneBlindIndex;
    @Column(length = 160)
    private String email;
    @Column(length = 120)
    private String company;
    @Column(length = 60)
    private String region;
    @Column(nullable = false, columnDefinition = "text")
    private String message = "";
    /** 평수·천장고 등 공간 조건. 자유 입력이며 개인정보가 아니다. */
    @Column(name = "space_info", length = 200)
    private String spaceInfo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.NEW;
    @Column(nullable = false, columnDefinition = "text")
    private String memo = "";
    @Column(name = "consent_at", nullable = false, updatable = false)
    private Instant consentAt;
    @Column(name = "marketing_consent_at")
    private Instant marketingConsentAt;
    @Column(name = "purge_at", nullable =false)
    private Instant purgeAt;
    @Column(name = "source_ip", columnDefinition = "inet")
    private String sourceIp;
    @Column(name = "user_agent", length = 400)
    private String userAgent;
    @ElementCollection
    @CollectionTable(name = "inquiry_product", joinColumns = @JoinColumn(name="inquiry_id"))
    @Column(name = "product_id", nullable = false)
    private Set<Long> productIds = new LinkedHashSet<>();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected Inquiry(){}
    public static Inquiry receive(
        InquiryType type,
        String name,
        String phoneEncrypted,
        byte[] phoneBlindIndex,
        Instant consentAt,
        Duration retention
    ) {
        if (consentAt == null) {
            throw new IllegalArgumentException("동의 없이 문의를 저장할 수 없습니다.");
        }
        if (phoneEncrypted == null || phoneEncrypted.isBlank()) {
            throw new IllegalArgumentException("연락처가 암호화되지 않았습니다.");
        }
        Inquiry inquiry = new Inquiry();
        inquiry.type = type;
        inquiry.name = name;
        inquiry.phoneEncrypted = phoneEncrypted;
        inquiry.phoneBlindIndex = phoneBlindIndex;
        inquiry.consentAt = consentAt;
        inquiry.purgeAt = consentAt.plus(retention);
        return inquiry;
    }
    public void applyOptionalFields(String email, String company, String region, String spaceInfo, String message){
        this.email = blankToNull(email);
        this.company = blankToNull(company);
        this.region = blankToNull(region);
        this.spaceInfo = blankToNull(spaceInfo);
        this.message = message == null ? "" : message;
    }
    public void applyMarketingConsent(Instant agreedAt) {
        this.marketingConsentAt = agreedAt;
    }
    public void applyRequestContext(String sourceIp, String userAgent){
        this.sourceIp = sourceIp;
        this.userAgent = (userAgent != null && userAgent.length() > 400)
            ? userAgent.substring(0, 400)
            : userAgent;
    }
    public void linkProducts(Set<Long> ids) {
        this.productIds.clear();
        if (ids != null) {
            this.productIds.addAll(ids);
        }
    }
    public void changeStatus(InquiryStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    public void writeMemo(String memo) {
        this.memo = memo == null ? "" : memo;
        this.updatedAt = Instant.now();
    }
    public String maskedName() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
    public boolean isPurgeDue(Instant now) {
        return purgeAt.isBefore(now);
    }
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
    public Long getId()                    { return id; }
    public InquiryType getType()           { return type; }
    public String getName()                { return name; }
    public String getPhoneEncrypted()      { return phoneEncrypted; }
    public String getEmail()               { return email; }
    public String getCompany()             { return company; }
    public String getRegion()              { return region; }
    public String getMessage()             { return message; }
    public InquiryStatus getStatus()       { return status; }
    public String getMemo()                { return memo; }
    public Instant getConsentAt()          { return consentAt; }
    public Instant getMarketingConsentAt() { return marketingConsentAt; }
    public Instant getPurgeAt()            { return purgeAt; }
    public Set<Long> getProductIds()       { return Set.copyOf(productIds); }
    public Instant getCreatedAt()          { return createdAt; }
    public String getSpaceInfo()           { return spaceInfo; }
}
