package kr.co.gymleco.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.service.machine.VerificationService;
import kr.co.gymleco.service.machine.VerificationView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/verify")
public class VerifyController {
    private final VerificationService verificationService;
    public VerifyController(VerificationService verificationService){
        this.verificationService = verificationService;
    }
    @PostMapping
    public VerificationView verify(@Valid @RequestBody VerifyRequest request, HttpServletRequest http){
        return verificationService.verify(request.serial(), request.modelCode(), http.getRemoteAddr(), http.getHeader("User-Agent"));
    }
}
