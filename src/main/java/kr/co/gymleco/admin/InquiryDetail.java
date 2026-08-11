package kr.co.gymleco.admin;

import kr.co.gymleco.domain.inquiry.Inquiry;

import java.time.Instant;
import java.util.Set;

public record InquiryDetail(
    Long id,
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
