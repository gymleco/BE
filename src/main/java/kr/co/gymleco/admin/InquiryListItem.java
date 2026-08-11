package kr.co.gymleco.admin;

import kr.co.gymleco.domain.inquiry.Inquiry;

import java.time.Instant;

public record InquiryListItem(
    Long id,
    String type,
    String maskedName,
    String company,
    String region,
    String status,
    Instant createdAt
) {
    public static InquiryListItem from(Inquiry inquiry){
        return new InquiryListItem(
            inquiry.getId(),
            inquiry.getType().label(),
            inquiry.maskedName(),
            inquiry.getCompany(),
            inquiry.getRegion(),
            inquiry.getStatus().name(),
            inquiry.getCreatedAt());
    }
}
