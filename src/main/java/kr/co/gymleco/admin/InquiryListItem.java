package kr.co.gymleco.admin;

import kr.co.gymleco.domain.inquiry.Inquiry;

import java.time.Instant;

public record InquiryListItem(
    Long id,
    /** 접수번호 — 카카오톡으로 온 번호와 맞춰 보는 값 (V10) */
    String referenceNo,
    /** 재문의면 같은 연락처의 원래 건 id, 아니면 null (V10) */
    Long duplicateOfId,
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
            inquiry.getReferenceNo(),
            inquiry.getDuplicateOfId(),
            inquiry.getType().label(),
            inquiry.maskedName(),
            inquiry.getCompany(),
            inquiry.getRegion(),
            inquiry.getStatus().name(),
            inquiry.getCreatedAt());
    }
}
