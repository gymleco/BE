package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.setting.SettingKey;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.SettingAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingController {
    private final SettingAdminService service;
    private final AdminUserRepository adminUsers;
    public AdminSettingController(SettingAdminService service, AdminUserRepository adminUsers){
        this.service = service;
        this.adminUsers = adminUsers;
    }
    @GetMapping
    public Map<String, String> all(){
        return service.all();
    }
    @GetMapping("/schema")
    public List<Map<String, Object>> schema(){
        return Arrays.stream(SettingKey.values())
            .map(k -> Map.<String, Object>of(
                "key", k.key(),
                "label", k.label(),
                "type", k.type().name(),
                "maxLength", k.maxLength(),
                "publicValue", k.isPublic()))
            .toList();
    }
    @PutMapping@ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody Map<String, String>body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http){
        service.updateAll(body, actor(jwt), http.getRemoteAddr());
    }
    private AdminUser actor(Jwt jwt){
        return adminUsers.findById(Long.valueOf(jwt.getSubject())).filter(AdminUser::isEnabled).orElseThrow(AuthenticationFailedException::new);
    }
}
