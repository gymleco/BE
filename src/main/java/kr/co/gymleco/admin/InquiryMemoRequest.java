package kr.co.gymleco.admin;

import jakarta.validation.constraints.Size;

public record InquiryMemoRequest(@Size(max = 2000) String memo) {
}
