package kr.co.gymleco.security.jwt;

import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 인증 쿠키 발급.
 *
 * 토큰은 응답 본문에 담지 않는다. Set-Cookie 로만 내려간다 —
 * 본문에 담으면 SPA 가 어딘가에 저장하게 되고, 그 순간
 * HttpOnly 로 얻은 이점이 사라진다.
 */
@Component
public class AuthCookies {

    public static final String ACCESS_COOKIE = CookieBearerTokenResolver.ACCESS_TOKEN_COOKIE;
    public static final String REFRESH_COOKIE = "gl_rt";

    private static final String REFRESH_PATH = "/api/admin/auth";
    private final GymlecoProperties.Auth config;

    public AuthCookies(GymlecoProperties properties) {
        this.config = properties.auth();
    }
    public ResponseCookie access(String token) {
        return build(ACCESS_COOKIE, token, "/", config.accessTtl());
    }
    public ResponseCookie refresh(String token) {
        return build(REFRESH_COOKIE, token, REFRESH_PATH, config.refreshTtl());
    }
    public ResponseCookie clearAccess() {
        return build(ACCESS_COOKIE, "", "/", Duration.ZERO);
    }

    public ResponseCookie clearRefresh() {
        return build(REFRESH_COOKIE, "", REFRESH_PATH, Duration.ZERO);
    }

    private ResponseCookie build(String name, String value,
                                 String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(config.cookieSecure())
            .sameSite("Strict")
            .path(path)
            .maxAge(maxAge);

        String domain = config.cookieDomain();
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
        return builder.build();
    }
}
