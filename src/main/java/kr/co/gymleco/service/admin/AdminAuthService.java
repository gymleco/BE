package kr.co.gymleco.service.admin;

import kr.co.gymleco.config.GymlecoProperties;
import kr.co.gymleco.domain.admin.*;
import kr.co.gymleco.security.jwt.JwtTokenService;
import kr.co.gymleco.security.login.LoginAttemptGuard;
import kr.co.gymleco.security.login.RefreshTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AdminAuthService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthService.class);

    /**
     * 존재하지 않는 계정에도 검증을 수행하기 위한 더미 해시.
     *
     * ★ 타이밍 공격 방어.
     *   계정이 없을 때 즉시 반환하면 응답이 빨라진다. 공격자는 그 차이로
     *   "이 아이디는 존재한다"를 알아낼 수 있다. 없는 계정에도 같은
     *   비용을 치르게 해 응답 시간을 맞춘다.
     */
    private static final String DUMMY_HASH =
        "$2b$12$........................................................";

    private final AdminUserRepository userRepository;
    private final AdminRefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final LoginAttemptGuard loginGuard;
    private final AuditLogger auditLogger;
    private final GymlecoProperties.Auth config;

    public AdminAuthService(AdminUserRepository userRepository,
                            AdminRefreshTokenRepository tokenRepository,
                            PasswordEncoder passwordEncoder,
                            JwtTokenService jwtTokenService,
                            LoginAttemptGuard loginGuard,
                            AuditLogger auditLogger,
                            GymlecoProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.loginGuard = loginGuard;
        this.auditLogger = auditLogger;
        this.config = properties.auth();
    }

    /* ═══════════════════ 로그인 ═══════════════════ */

    @Transactional
    public IssuedTokens login(String username, String rawPassword,
                              String ip, String userAgent) {

        if (loginGuard.isBlocked(ip)) {
            auditLogger.loginBlocked(username, ip);
            throw new AuthenticationFailedException();
        }

        // 실패할수록 느려진다. 사람은 못 느끼고 자동화 도구는 처리량이 떨어진다.
        sleep(loginGuard.penaltyFor(username));

        AdminUser user = userRepository
            .findByUsernameAndEnabledTrue(username)
            .orElse(null);

        /*
         * ★ 계정 없음과 비밀번호 틀림을 구분하지 않는다.
         *
         * 메시지를 나누면 "이 아이디는 존재한다"가 새어 나가고,
         * 공격자는 유효한 아이디 목록부터 만든 뒤 비밀번호만 두드린다.
         */
        String hash = (user == null) ? DUMMY_HASH : user.getPasswordHash();
        boolean matches = passwordEncoder.matches(rawPassword, hash);

        if (user == null || !matches) {
            loginGuard.recordFailure(ip, username);
            auditLogger.loginFailed(username, ip, userAgent);
            throw new AuthenticationFailedException();
        }

        // TODO: totpEnabled 인 경우 여기서 TOTP 코드를 검증한다

        Instant now = Instant.now();
        String previousIp = user.recordLogin(now, ip);
        loginGuard.recordSuccess(ip, username);

        /*
         * 새 IP 로그인 알림 (§10).
         * 비밀번호가 털려도 대표님이 즉시 알아챌 수 있는 마지막 신호다.
         */
        if (previousIp != null && !previousIp.equals(ip)) {
            auditLogger.loginFromNewIp(user, ip, previousIp);
        }

        auditLogger.loginSucceeded(user, ip, userAgent);
        return issueTokens(user, ip, userAgent, now);
    }

    /* ═══════════════════ 재발급 (회전) ═══════════════════ */

    @Transactional
    public IssuedTokens refresh(String rawRefreshToken, String ip, String userAgent) {
        byte[] hash = RefreshTokens.hash(rawRefreshToken);
        Instant now = Instant.now();

        AdminRefreshToken stored = tokenRepository
            .findByTokenHash(hash)
            .orElseThrow(AuthenticationFailedException::new);

        /*
         * ★ 재사용 감지.
         *
         * 이미 회전된 토큰이 다시 들어왔다는 것은, 정상 사용자와 공격자가
         * 같은 토큰을 나눠 가졌다는 뜻이다. 둘 중 누가 진짜인지 알 수 없으므로
         * 해당 계정의 모든 세션을 끊는다.
         *
         * 관리자는 다시 로그인하면 되지만, 공격자는 비밀번호를 모르면
         * 돌아올 수 없다. 불편을 감수하고 안전을 택하는 지점이다.
         */
        if (stored.isAlreadyRotated()) {
            int revoked = tokenRepository.revokeAllForUser(
                stored.getAdminUser().getId(), now);
            auditLogger.refreshTokenReuseDetected(stored.getAdminUser(), ip, revoked);
            log.warn("Refresh Token 재사용 감지 — 사용자 {} 의 세션 {}개를 무효화했습니다.",
                stored.getAdminUser().getId(), revoked);
            throw new AuthenticationFailedException();
        }

        if (!stored.isActive(now)) {
            throw new AuthenticationFailedException();
        }

        AdminUser user = stored.getAdminUser();
        if (!user.isEnabled()) {
            throw new AuthenticationFailedException();
        }

        IssuedTokens issued = issueTokens(user, ip, userAgent, now);
        // 새 토큰을 발급한 뒤 이전 토큰을 "교체됨"으로 표시한다
        stored.replaceWith(issued.refreshEntity(), now);

        return issued;
    }

    /* ═══════════════════ 로그아웃 ═══════════════════ */

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        tokenRepository.findByTokenHash(RefreshTokens.hash(rawRefreshToken))
            .ifPresent(token -> token.revoke(Instant.now()));
    }

    /* ═══════════════════ 내부 ═══════════════════ */

    private IssuedTokens issueTokens(AdminUser user, String ip,
                                     String userAgent, Instant now) {
        String accessToken = jwtTokenService.issueAccessToken(
            user.getId(), user.getUsername(), user.getRole().name());

        String rawRefresh = RefreshTokens.generate();
        AdminRefreshToken refreshEntity = tokenRepository.save(
            AdminRefreshToken.issue(
                user,
                RefreshTokens.hash(rawRefresh),
                now.plus(config.refreshTtl()),
                ip,
                userAgent));

        return new IssuedTokens(accessToken, rawRefresh, refreshEntity, user);
    }

    private void sleep(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 원본 refresh 는 쿠키로만 나가고 서버에는 남지 않는다. */
    public record IssuedTokens(
        String accessToken,
        String rawRefreshToken,
        AdminRefreshToken refreshEntity,
        AdminUser user
    ) {
    }
}
