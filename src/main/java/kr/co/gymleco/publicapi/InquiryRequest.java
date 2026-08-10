package kr.co.gymleco.publicapi;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.gymleco.domain.inquiry.InquiryType;
import kr.co.gymleco.service.inquiry.InquiryCommand;

import java.util.List;

/**
 * 요청 본문을 InquiryCommand 로 직접 역직렬화하지 않는 이유:
 * 커맨드에는 sourceIp·userAgent 가 있다. 본문에서 받으면 요청자가
 * 자기 IP 를 위조해 rate limit 을 우회한다 (mass assignment).
 * 경계 전용 DTO 를 두고, 서버가 아는 값은 서버가 채운다.
 */
public record InquiryRequest(
    @NotNull InquiryType type,

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 80)
    String name,

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Size(max = 40)
    String phone,

    @Size(max = 160) String email,
    @Size(max = 120) String company,
    @Size(max = 60) String region,
    @Size(max = 200) String spaceInfo,

    // 화면의 체크박스 수보다 넉넉하게. 수백 개를 밀어 넣는 봇만 거른다.
    @Size(max = 30) List<@Size(max = 140) String> productSlugs,

    @Size(max = 4000) String message,

    @AssertTrue(message = "개인정보 수집·이용에 동의해 주세요.")
    boolean privacyConsent,

    boolean marketingConsent
) {
    public InquiryCommand toCommand(String sourceIp, String userAgent) {
        return new InquiryCommand(
            type, name, phone, email, company, region, spaceInfo, message,
            productSlugs, privacyConsent, marketingConsent,
            sourceIp, userAgent);
    }
}
