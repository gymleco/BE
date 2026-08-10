package kr.co.gymleco.service.inquiry;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.gymleco.domain.inquiry.InquiryType;

import java.util.List;
public record InquiryCommand(
    InquiryType type,
    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 80)
    String name,
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Size(max = 40)
    String phone,
    @Size(max = 160) String email,
    @Size(max = 120) String company,
    @Size(max = 60) String region,
    @Size(max = 4000) String message,
    List<Long> productIds,
    @AssertTrue(message = "개인정보 수집·이용에 동의해 주세요.")
    boolean privacyConsent,

    boolean marketingConsent,

    String sourceIp,
    String userAgent
) {
}
