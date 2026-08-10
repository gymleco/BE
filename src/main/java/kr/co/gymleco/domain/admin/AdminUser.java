package kr.co.gymleco.domain.admin;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "admin_user")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 60)
    private String displayName = "";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role = AdminRole.ADMIN;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(name = "totp_secret_encrypted", columnDefinition = "text")
    private String totpSecretEncrypted;
    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled = false;
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    @Column(name = "last_login_ip", columnDefinition = "inet")
    private String lastLoginIp;
    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt = Instant.now();
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected AdminUser(){}
    public static AdminUser create(String username, String bcryptHash, String displayName, AdminRole role) {
        AdminUser user = new AdminUser();
        user.username = username;
        user.passwordHash = bcryptHash;
        user.displayName = displayName == null ? "" : displayName;
        user.role = role;
        return user;
    }
    public String recordLogin(Instant at, String ip) {
        String previousIp = this.lastLoginIp;
        this.lastLoginAt = at;
        this.lastLoginIp = ip;
        this.updatedAt = at;
        return previousIp;
    }
    public void changePassword(String bcryptHash) {
        this.passwordHash = bcryptHash;
        this.passwordChangedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public void enableTotp(String encryptedSecret) {
        this.totpSecretEncrypted = encryptedSecret;
        this.totpEnabled = true;
        this.updatedAt = Instant.now();
    }
    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }
    public boolean isAdmin() {
        return role == AdminRole.ADMIN;
    }
    public Long getId()                  { return id; }
    public String getUsername()          { return username; }
    public String getPasswordHash()      { return passwordHash; }
    public String getDisplayName()       { return displayName; }
    public AdminRole getRole()           { return role; }
    public boolean isEnabled()           { return enabled; }
    public boolean isTotpEnabled()       { return totpEnabled; }
    public String getTotpSecretEncrypted() { return totpSecretEncrypted; }
    public Instant getLastLoginAt()      { return lastLoginAt; }
}
