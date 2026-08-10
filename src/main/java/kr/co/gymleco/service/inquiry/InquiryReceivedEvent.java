package kr.co.gymleco.service.inquiry;

import kr.co.gymleco.domain.inquiry.InquiryType;

public record InquiryReceivedEvent (
    Long inquiryId,
    InquiryType type,
    String maskedName,
    String company,
    String region
){
}
