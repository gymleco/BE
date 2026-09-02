package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.banner.Banner;
import kr.co.gymleco.domain.banner.BannerPosition;
import kr.co.gymleco.domain.banner.SectionMedia;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.BannerAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerAdminService service;
    private final AdminUserRepository adminUsers;

    public AdminBannerController(BannerAdminService service,
                                 AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }

    /**
     * 드롭다운 목록.
     *
     * 화면에 값을 하드코딩하면 위치를 추가할 때 두 곳을 고쳐야 하고,
     * 값이 어긋나면 배너가 어디에도 안 뜨는데 화면에서는 알아채기 어렵다.
     * 서버가 정답을 준다.
     */
    @GetMapping("/banner-positions")
    public List<Map<String, String>> positions() {
        return Arrays.stream(BannerPosition.values())
            .map(p -> Map.of("value", p.name(), "label", p.label()))
            .toList();
    }

    @GetMapping("/banners")
    public List<BannerAdminResponse> list() {
        return service.list().stream().map(BannerAdminResponse::from).toList();
    }

    @PostMapping("/banners")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody BannerRequest body,
                                    @AuthenticationPrincipal Jwt jwt,
                                    HttpServletRequest http) {
        return Map.of("id", service.create(body, actor(jwt), http.getRemoteAddr()));
    }

    @PutMapping("/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id,
                       @Valid @RequestBody BannerRequest body,
                       @AuthenticationPrincipal Jwt jwt,
                       HttpServletRequest http) {
        service.update(id, body, actor(jwt), http.getRemoteAddr());
    }

    @DeleteMapping("/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal Jwt jwt,
                       HttpServletRequest http) {
        service.delete(id, actor(jwt), http.getRemoteAddr());
    }

    /* ── 섹션 이미지 ── */

    @GetMapping("/section-media")
    public List<SectionMediaAdminResponse> sections() {
        return service.listSections().stream().map(SectionMediaAdminResponse::from).toList();
    }

    @PutMapping("/section-media/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void putSection(@PathVariable String key,
                           @Valid @RequestBody SectionMediaRequest body,
                           @AuthenticationPrincipal Jwt jwt,
                           HttpServletRequest http) {
        service.putSection(key, body, actor(jwt), http.getRemoteAddr());
    }

    /**
     * JWT subject 에는 username 이 아니라 계정 id 가 들어 있다.
     * isEnabled 를 반드시 건다 — 비활성 계정의 만료 전 토큰으로
     * 배너가 바뀌면 안 된다.
     */
    private AdminUser actor(Jwt jwt) {
        return adminUsers.findById(Long.valueOf(jwt.getSubject()))
            .filter(AdminUser::isEnabled)
            .orElseThrow(AuthenticationFailedException::new);
    }
}
