package kr.co.gymleco.admin;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.gymleco.domain.admin.AdminUser;
import kr.co.gymleco.domain.admin.AdminUserRepository;
import kr.co.gymleco.domain.audit.AuditAction;
import kr.co.gymleco.service.admin.AuditLogger;
import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUploadController {

    private final ImageUploadService uploadService;
    private final AdminUserRepository adminUsers;
    private final AuditLogger auditLogger;

    public AdminUploadController(ImageUploadService uploadService,
                                 AdminUserRepository adminUsers,
                                 AuditLogger auditLogger) {
        this.uploadService = uploadService;
        this.adminUsers = adminUsers;
        this.auditLogger = auditLogger;
    }

    @PostMapping("/image")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadService.UploadResult image(
        @RequestPart("file") MultipartFile file,
        @AuthenticationPrincipal Jwt jwt,
        HttpServletRequest http) {

        ImageUploadService.UploadResult result = uploadService.upload(file);

        auditLogger.contentChanged(actor(jwt), AuditAction.PRODUCT_UPDATED,
            "Image", result.key(), "이미지 업로드", http.getRemoteAddr());

        return result;
    }

    private AdminUser actor(Jwt jwt) {
        return adminUsers.findById(Long.valueOf(jwt.getSubject()))
            .filter(AdminUser::isEnabled)
            .orElseThrow(AuthenticationFailedException::new);
    }
}
