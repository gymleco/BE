package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.security.jwt.AuthCookies;
import kr.co.gymleco.service.admin.AdminAuthService;
import kr.co.gymleco.service.admin.AuditLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AdminAuthService authService;
    private final AuthCookies cookies;
    public AdminAuthController(AdminAuthService authService, AuthCookies cookies){
        this.authService = authService;
        this.cookies = cookies;
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest request, HttpServletRequest http){
        var issued = authService.login(
            request.username(),
            request.password(),
            http.getRemoteAddr(),
            http.getHeader("User-Agent"));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookies.access(issued.accessToken()).toString()).header(HttpHeaders.SET_COOKIE,cookies.refresh(issued.rawRefreshToken()).toString())
            .body(Map.of(
                "username", issued.user().getUsername(),
                "displayName", issued.user().getDisplayName(),
                "role", issued.user().getRole().name()));
    }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
        @CookieValue(name = AuthCookies.REFRESH_COOKIE, required = false)
        String refreshToken, HttpServletRequest http){
        var issued = authService.refresh(refreshToken, http.getRemoteAddr(), http.getHeader("User-Agent"));
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookies.access(issued.accessToken()).toString())
            .header(HttpHeaders.SET_COOKIE, cookies.refresh(issued.rawRefreshToken()).toString())
            .body(Map.of("username", issued.user().getUsername()));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @CookieValue(name = AuthCookies.REFRESH_COOKIE, required = false)
        String refreshToken){
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookies.clearAccess().toString())
            .header(HttpHeaders.SET_COOKIE, cookies.clearRefresh().toString())
            .build();
    }
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt){
        return Map.of(
            "username", jwt.getClaimAsString("username"),
            "role", jwt.getClaimAsString("role"));
    }
}
