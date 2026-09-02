package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.product.ProductType;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.ProductAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductAdminService service;
    private final AdminUserRepository adminUsers;

    public AdminProductController(ProductAdminService service,
                                  AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }

    @GetMapping
    public List<ProductAdminResponse> list(
        @RequestParam(required = false) ProductType type) {
        return service.list(type);
    }

    @GetMapping("/{id}")
    public ProductAdminResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody ProductCreateRequest request,
                                    @AuthenticationPrincipal Jwt jwt,
                                    HttpServletRequest http) {
        Long id = service.create(request, actor(jwt), http.getRemoteAddr());
        return Map.of("id", id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id,
                       @Valid @RequestBody ProductRequest request,
                       @AuthenticationPrincipal Jwt jwt,
                       HttpServletRequest http) {
        service.update(id, request, actor(jwt), http.getRemoteAddr());
    }

    @PatchMapping("/{id}/visibility")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void visibility(@PathVariable Long id,
                           @RequestBody Map<String, Boolean> body,
                           @AuthenticationPrincipal Jwt jwt,
                           HttpServletRequest http) {
        Boolean visible = body.get("visible");
        // 키가 없거나 오타("visable")면 Boolean.TRUE.equals(null) 이 false 가 돼서
        // "숨김" 이 조용히 실행된다. 안 보내면 못 알아들었다고 말해야 한다.
        if (visible == null) throw new IllegalArgumentException("visible 값이 필요합니다.");
        service.changeVisibility(id, visible, actor(jwt), http.getRemoteAddr());
    }

    /** 드래그로 순서를 바꾼 결과를 한 번에 받는다. { "12": 0, "7": 1, ... } */
    @PutMapping("/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody Map<Long, Integer> orders,
                        @AuthenticationPrincipal Jwt jwt,
                        HttpServletRequest http) {
        service.reorder(orders, actor(jwt), http.getRemoteAddr());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal Jwt jwt,
                       HttpServletRequest http) {
        service.delete(id, actor(jwt), http.getRemoteAddr());
    }

    /**
     * JWT 의 subject 로 실제 계정을 찾는다. 감사 로그에 남길 주체다.
     *
     * ★ 계정을 못 찾아도 null 을 그냥 넘기지 않는다.
     *
     *   null 을 넘기면 감사 로그에 "누가 했는지 모르는 기록" 이 남는다.
     *   사고 조사 때 가장 필요한 정보가 비어 있게 된다.
     *
     *   토큰은 유효한데 계정이 삭제·비활성된 경우이므로 401 로 돌린다.
     *   서버 오류(500)가 아니다 — 세션이 더 이상 유효하지 않다는 뜻이고,
     *   SPA 는 401 을 받아 로그인 화면으로 보내면 된다.
     */
    private AdminUser actor(Jwt jwt) {
        return adminUsers.findById(Long.valueOf(jwt.getSubject()))
                .filter(AdminUser::isEnabled)
                .orElseThrow(AuthenticationFailedException::new);
    }
}
