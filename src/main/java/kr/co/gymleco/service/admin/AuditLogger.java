package kr.co.gymleco.service.admin;

import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.audit.AdminAuditLog;
import kr.co.gymleco.domain.audit.AdminAuditLogRepository;
import kr.co.gymleco.domain.audit.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 감사 로그 기록.
 *
 * 사고가 났을 때 유일한 단서다 (기획서 §13).
 * "언제부터 뚫렸는지", "무엇을 가져갔는지"를 답할 수 있는 유일한 자료이고,
 * 개인정보 유출 신고 시에도 이 기록이 근거가 된다.
 */
@Service
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final AdminAuditLogRepository repository;

    /**
     * ★ @Transactional 대신 TransactionTemplate 을 쓰는 이유
     *
     * @Transactional 은 Spring 프록시를 통해 호출될 때만 적용된다.
     * 이 클래스처럼 public 메서드가 같은 클래스의 write() 를 부르면
     * 프록시를 거치지 않아 애너테이션이 조용히 무시된다(self-invocation).
     *
     * 그러면 로그인 실패 기록이 바깥 트랜잭션 롤백과 함께 사라진다 —
     * 무차별 대입을 당하는 동안 로그가 텅 비는, 정확히 막으려던 상황이다.
     *
     * TransactionTemplate 은 호출 경로와 무관하게 새 트랜잭션을 연다.
     */
    private final TransactionTemplate requiresNew;

    public AuditLogger(AdminAuditLogRepository repository,
                       PlatformTransactionManager transactionManager) {
        this.repository = repository;

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(
            TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNew = template;
    }

    /* ═══════════════════ 인증 ═══════════════════ */

    public void loginSucceeded(AdminUser user, String ip, String userAgent) {
        write(AdminAuditLog.of(user, null, AuditAction.LOGIN_SUCCEEDED,
            "", ip, userAgent));
    }

    public void loginFailed(String username, String ip, String userAgent) {
        // 사용자 객체가 없다 — 존재하지 않는 계정일 수도 있다.
        // ★ 입력된 비밀번호는 절대 남기지 않는다.
        write(AdminAuditLog.of(null, username, AuditAction.LOGIN_FAILED,
            "", ip, userAgent));
    }

    public void loginBlocked(String username, String ip) {
        write(AdminAuditLog.of(null, username, AuditAction.LOGIN_BLOCKED,
            "IP 시도 한도 초과", ip, null));
    }

    public void loginFromNewIp(AdminUser user, String ip, String previousIp) {
        write(AdminAuditLog.of(user, null, AuditAction.LOGIN_NEW_IP,
            "직전 접속 IP 와 다름: " + previousIp, ip, null));
    }

    public void refreshTokenReuseDetected(AdminUser user, String ip, int revokedCount) {
        write(AdminAuditLog.of(user, null, AuditAction.REFRESH_TOKEN_REUSE,
            "재사용 감지로 세션 " + revokedCount + "개 무효화", ip, null));
    }

    public void logout(AdminUser user, String ip) {
        write(AdminAuditLog.of(user, null, AuditAction.LOGOUT, "", ip, null));
    }

    /* ═══════════════════ 개인정보 ═══════════════════ */

    /**
     * 문의 상세 열람 = 연락처 복호화.
     * 유출 사고 시 "무엇이 언제 노출됐는가"를 답할 근거가 된다.
     */
    public void inquiryViewed(AdminUser user, Long inquiryId, String ip) {
        write(AdminAuditLog.of(user, null, AuditAction.INQUIRY_VIEWED, "", ip, null)
            .withTarget("Inquiry", inquiryId));
    }

    /**
     * CSV 내보내기.
     * ★ 컬럼 암호화를 무력화하는 유일한 통로다. 몇 건이 나갔는지 반드시 남긴다.
     */
    public void inquiryExported(AdminUser user, int count,
                                String filterDescription, String ip) {
        write(AdminAuditLog.of(user, null, AuditAction.INQUIRY_EXPORTED,
            count + "건 내보냄 · 조건: " + filterDescription, ip, null));
    }

    public void inquiryStatusChanged(AdminUser user, Long inquiryId,
                                     String from, String to, String ip) {
        write(AdminAuditLog.of(user, null, AuditAction.INQUIRY_STATUS_CHANGED,
                from + " → " + to, ip, null)
            .withTarget("Inquiry", inquiryId));
    }

    /** 자동 파기 배치. 건수만 남기고 누구였는지는 남기지 않는다. */
    public void inquiryPurged(int count) {
        write(AdminAuditLog.of(null, "system", AuditAction.INQUIRY_PURGED,
            count + "건 자동 파기", null, null));
    }

    /* ═══════════════════ 콘텐츠 ═══════════════════ */

    public void contentChanged(AdminUser user, AuditAction action,
                               String targetType, Object targetId,
                               String detail, String ip) {
        write(AdminAuditLog.of(user, null, action, detail, ip, null)
            .withTarget(targetType, targetId));
    }

    /* ═══════════════════ 기록 ═══════════════════ */

    private void write(AdminAuditLog entry) {
        try {
            // 별도 트랜잭션에서 즉시 커밋한다. 바깥이 롤백돼도 기록은 남는다.
            requiresNew.executeWithoutResult(status -> repository.save(entry));

        } catch (RuntimeException e) {
            /*
             * 감사 로그 실패가 본래 작업을 막아서는 안 된다.
             * 로그를 못 남겼다고 로그인까지 실패시키면, 로그 테이블 하나가
             * 서비스 전체를 멈추는 단일 장애점이 된다.
             */
            log.error("감사 로그 기록 실패: action={} 사유={}",
                entry.getAction(), e.getMessage());
        }
    }
}
