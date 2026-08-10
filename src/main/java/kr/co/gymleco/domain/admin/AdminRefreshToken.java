package kr.co.gymleco.domain.admin;

import jakarta.persistence.*;

import java.time.Instant;
@Entity
@Table(name = "admin_refresh_token")
public class AdminRefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private AdminUser adminUser;
    /** SHA-256 */
    @Column(name = "token_hash", nullable = false, unique = true)
    private byte[] tokenHash;
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by")
    private AdminRefreshToken replacedBy;
    @Column(name = "user_agent", length = 400)
    private String userAgent;
    @Column(columnDefinition = "inet")
    private String ip;
    protected AdminRefreshToken() {
    }
    public static AdminRefreshToken issue(AdminUser user, byte[] tokenHash,
                                          Instant expiresAt,
                                          String ip, String userAgent) {
        AdminRefreshToken token = new AdminRefreshToken();
        token.adminUser = user;
        token.tokenHash = tokenHash;
        token.expiresAt = expiresAt;
        token.ip = ip;
        token.userAgent = userAgent;
        return token;
    }
    public void revoke(Instant at) {
        if (this.revokedAt == null) {
            this.revokedAt = at;
        }
    }
    public void replaceWith(AdminRefreshToken next, Instant at) {
        this.replacedBy = next;
        revoke(at);
    }
    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
    public boolean isAlreadyRotated() {
        return replacedBy != null;
    }
    public Long getId()            { return id; }
    public AdminUser getAdminUser() { return adminUser; }
    public Instant getExpiresAt()  { return expiresAt; }
    public Instant getRevokedAt()  { return revokedAt; }
}
