package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotNull;
import kr.co.gymleco.domain.inquiry.InquiryStatus;

public record InquiryStatusRequest(@NotNull InquiryStatus status) {
}
