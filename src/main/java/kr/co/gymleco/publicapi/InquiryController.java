package kr.co.gymleco.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.gymleco.service.inquiry.InquiryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    /*
     * getRemoteAddr() 로 충분한 이유: application.yml 의
     * forward-headers-strategy: native 가 X-Forwarded-For 를 이미
     * 반영해 준다. 여기서 헤더를 직접 파싱하면 신뢰 로직이 두 벌이 된다.
     *
     * 저장된 id 를 돌려주지 않는다 — 공개 응답에 내부 식별자를 노출할
     * 이유가 없고, FE 프록시도 본문을 쓰지 않는다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Boolean> receive(@Valid @RequestBody InquiryRequest request,
                                        HttpServletRequest http) {
        inquiryService.receive(request.toCommand(
            http.getRemoteAddr(),
            http.getHeader("User-Agent")));
        return Map.of("ok", true);
    }
}
