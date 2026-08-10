package kr.co.gymleco.domain.audit;

import jakarta.persistence.*;
import kr.co.gymleco.domain.admin.AdminUser;

import java.time.Instant;
@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id")
    private AdminUser adminUser;
    @Column(name = "admin_username", nullable = false, length = 60)
    private String adminUsername = "";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AuditAction action;
    @Column(name = "target_type", length = 40)
    private String targetType;
    @Column(name = "target_id", length = 60)
    private String targetId;
    @Column(nullable = false, columnDefinition = "text")
    private String detail = "";
    @Column(columnDefinition = "inet")
    private String ip;
    @Column(name = "user_agent", length = 400)
    private String userAgent;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    protected AdminAuditLog() {
    }
    public static AdminAuditLog of(AdminUser user, String usernameFallback,
                                   AuditAction action, String detail,
                                   String ip, String userAgent) {
        AdminAuditLog entry = new AdminAuditLog();
        entry.adminUser = user;
        entry.adminUsername = (user != null)
            ? user.getUsername()
            : (usernameFallback == null ? "" : usernameFallback);
        entry.action = action;
        entry.detail = detail == null ? "" : detail;
        entry.ip = ip;
        entry.userAgent = userAgent;
        return entry;
    }
    public AdminAuditLog withTarget(String targetType, Object targetId) {
        this.targetType = targetType;
        this.targetId = targetId == null ? null : String.valueOf(targetId);
        return this;
    }
    public Long getId()             { return id; }
    public String getAdminUsername() { return adminUsername; }
    public AuditAction getAction()  { return action; }
    public String getTargetType()   { return targetType; }
    public String getTargetId()     { return targetId; }
    public String getDetail()       { return detail; }
    public String getIp()           { return ip; }
    public Instant getCreatedAt()   { return createdAt; }
}
