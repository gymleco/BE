package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.used.UsedStatus;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.UsedAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/used")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsedController {
    private final UsedAdminService service;
    private final AdminUserRepository adminUsers;
    public AdminUsedController(UsedAdminService service, AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }
    @GetMapping
    public List<UsedItemAdminResponse> list(@RequestParam(required = false) UsedStatus status) {return service.list(status);
    }
    @GetMapping("/{id}")
    public UsedItemAdminResponse detail(@PathVariable Long id) {return service.detail(id);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody UsedItemCreateRequest request, @AuthenticationPrincipal Jwt jwt,HttpServletRequest http) {return Map.of("id", service.create(request, actor(jwt), http.getRemoteAddr()));
    }
    @PutMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Long id, @Valid @RequestBody UsedItemRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.update(id, body, actor(jwt), http.getRemoteAddr());
    }
    @PatchMapping("/{id}/status") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable Long id, @Valid @RequestBody UsedStatusRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.changeStatus(id, body.status(), actor(jwt), http.getRemoteAddr());
    }
    @PatchMapping("/{id}/visibility")@ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeVisibility(@PathVariable Long id, @RequestBody Map<String, Boolean> body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        Boolean visible = body.get("visible");
        // 키가 없거나 오타("visable")면 Boolean.TRUE.equals(null) 이 false 가 돼서
        // "숨김" 이 조용히 실행된다. 안 보내면 못 알아들었다고 말해야 한다.
        if (visible == null) throw new IllegalArgumentException("visible 값이 필요합니다.");
        service.changeVisibility(id, visible, actor(jwt), http.getRemoteAddr());
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.delete(id, actor(jwt), http.getRemoteAddr());
    }
    private AdminUser actor(Jwt jwt) {return adminUsers.findById(Long.valueOf(jwt.getSubject())).filter(AdminUser::isEnabled).orElseThrow(AuthenticationFailedException::new);
    }
}
