package kr.co.gymleco.admin;

import kr.co.gymleco.domain.inquiry.Inquiry;

import java.time.Instant;
import java.util.Set;

public record InquiryDetail(
    Long id,
    /** 접수번호 — 카카오톡으로 온 번호와 맞춰 보는 값 (V10) */
    String referenceNo,
    /** 재문의면 같은 연락처의 원래 건 id, 아니면 null (V10) */
    Long duplicateOfId,
    String type,
    String name,
    String phone,
    String email,
    String company,
    String region,
    String spaceInfo,
    String message,
    Set<Long> productIds,
    String status,
    String memo,
    Instant consentAt,
    Instant marketingConsentAt,
    Instant purgeAt,
    Instant createdAt
) {
    public static InquiryDetail of(Inquiry inquiry, String decryptedPhone) {
        return new InquiryDetail(
            inquiry.getId(),
            inquiry.getReferenceNo(),
            inquiry.getDuplicateOfId(),
            inquiry.getType().label(),
            inquiry.getName(),
            decryptedPhone,
            inquiry.getEmail(),
            inquiry.getCompany(),
            inquiry.getRegion(),
            inquiry.getSpaceInfo(),
            inquiry.getMessage(),
            inquiry.getProductIds(),
            inquiry.getStatus().name(),
            inquiry.getMemo(),
            inquiry.getConsentAt(),
            inquiry.getMarketingConsentAt(),
            inquiry.getPurgeAt(),
            inquiry.getCreatedAt());
    }
}
