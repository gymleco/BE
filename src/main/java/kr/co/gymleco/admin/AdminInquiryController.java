package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.inquiry.InquiryStatus;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.InquiryAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/inquiries")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {
    private final InquiryAdminService service;
    private final AdminUserRepository adminUsers;
    public AdminInquiryController(InquiryAdminService service,
                                  AdminUserRepository adminUsers) {
        this.service = service;
        this.adminUsers = adminUsers;
    }

    @GetMapping
    public Page<InquiryListItem> list(
        @RequestParam(required = false) InquiryStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return service.list(status, PageRequest.of(page, Math.min(size, 100)));
    }
    @GetMapping("/{id}")
    public InquiryDetail detail(@PathVariable Long id,
                                @AuthenticationPrincipal Jwt jwt,
                                HttpServletRequest http) {
        return service.detail(id, actor(jwt), http.getRemoteAddr());
    }
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(@PathVariable Long id,
                             @Valid @RequestBody InquiryStatusRequest request,
                             @AuthenticationPrincipal Jwt jwt,
                             HttpServletRequest http) {
        service.changeStatus(id, request.status(), actor(jwt), http.getRemoteAddr());
    }
    @PutMapping("/{id}/memo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void writeMemo(@PathVariable Long id,
                          @Valid @RequestBody InquiryMemoRequest request,
                          @AuthenticationPrincipal Jwt jwt,
                          HttpServletRequest http) {
        service.writeMemo(id, request.memo(), actor(jwt), http.getRemoteAddr());
    }

    /**
     * CSV 내보내기.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
        @RequestParam(required = false) InquiryStatus status,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal Jwt jwt,
        HttpServletRequest http) {InquiryAdminService.CsvExport export = service.exportCsv(status, from, to, actor(jwt), http.getRemoteAddr());
        ContentDisposition disposition = ContentDisposition.attachment()
            .filename(export.filename(), StandardCharsets.UTF_8)
            .build();
        return ResponseEntity.ok()
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .header("X-Export-Count", String.valueOf(export.count()))
            .header("X-Export-Truncated", String.valueOf(export.truncated()))
            .body(export.content());
    }
    /**
     * JWT 의 subject 로 실제 계정을 찾는다. 감사 로그에 남길 주체다.
     *
     * ★ subject 에는 username 이 아니라 계정 id 가 들어 있다
     *   (JwtTokenService:42). username 으로 찾으면 영원히 401 이다.
     *
     * ★ isEnabled 를 반드시 건다.
     *   비활성 처리된 계정이라도 만료 전 토큰은 아직 유효하다.
     *   이 컨트롤러가 다루는 것이 전부 개인정보라, 내보낸 뒤 계정을
     *   막았는데 그 토큰으로 계속 읽히면 차단이 무의미해진다.
     */
    private AdminUser actor(Jwt jwt) {
        return adminUsers.findById(Long.valueOf(jwt.getSubject()))
            .filter(AdminUser::isEnabled)
            .orElseThrow(AuthenticationFailedException::new);
    }
}
