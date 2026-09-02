package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotNull;
import kr.co.gymleco.domain.inquiry.InquiryStatus;

public record InquiryStatusRequest(@NotNull(message = "상태를 선택해 주세요.") InquiryStatus status) {
}
