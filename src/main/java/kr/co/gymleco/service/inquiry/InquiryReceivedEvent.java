package kr.co.gymleco.service.inquiry;

import kr.co.gymleco.domain.inquiry.InquiryType;

public record InquiryReceivedEvent(
    Long inquiryId,
    String referenceNo,
    /** 재문의면 원래 건의 접수번호, 아니면 null */
    String originalReferenceNo,
    InquiryType type,
    String maskedName,
    String company,
    String region
) {
    public boolean isRepeat() {
        return originalReferenceNo != null;
    }
}
