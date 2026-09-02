package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.support.Faq;
import kr.co.gymleco.domain.support.Notice;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.SupportAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/support")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportController {
    private final SupportAdminService service;
    private final AdminUserRepository adminUsers;
    public AdminSupportController(SupportAdminService service, AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }
    @GetMapping("/faq")
    public List<FaqAdminResponse> listFaq() {return service.listFaq().stream().map(FaqAdminResponse::from).toList();}
    @PostMapping("/faq") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createFaq(@Valid @RequestBody FaqRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {return Map.of("id", service.createFaq(body, actor(jwt), http.getRemoteAddr()));}
    @PutMapping("/faq/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.updateFaq(id, body, actor(jwt), http.getRemoteAddr());}
    @DeleteMapping("/faq/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteFaq(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.deleteFaq(id, actor(jwt), http.getRemoteAddr());}
    @GetMapping("/notice") public List<NoticeAdminResponse> listNotice() {return service.listNotice().stream().map(NoticeAdminResponse::summary).toList();}
    @GetMapping("/notice/{id}")
    public NoticeAdminResponse detailNotice(@PathVariable Long id) {return NoticeAdminResponse.detail(service.detailNotice(id));}
    @PostMapping("/notice") @ResponseStatus(HttpStatus.CREATED) public Map<String, Long> createNotice(@Valid @RequestBody NoticeRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {return Map.of("id", service.createNotice(body, actor(jwt), http.getRemoteAddr()));}
    @PutMapping("/notice/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeRequest body, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.updateNotice(id, body, actor(jwt), http.getRemoteAddr());}
    @DeleteMapping("/notice/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteNotice(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {service.deleteNotice(id, actor(jwt), http.getRemoteAddr());}
    private AdminUser actor(Jwt jwt) {return adminUsers.findById(Long.valueOf(jwt.getSubject())).filter(AdminUser::isEnabled).orElseThrow(AuthenticationFailedException::new);}
}
