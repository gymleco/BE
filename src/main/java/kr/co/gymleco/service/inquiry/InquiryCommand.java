package kr.co.gymleco.service.inquiry;

import kr.co.gymleco.domain.inquiry.InquiryType;

import java.util.List;

public record InquiryCommand(
    InquiryType type,
    String name,
    String phone,
    String email,
    String company,
    String region,
    String spaceInfo,
    String message,
    List<String> productSlugs,
    boolean privacyConsent,
    boolean marketingConsent,
    String sourceIp,
    String userAgent
) {
}