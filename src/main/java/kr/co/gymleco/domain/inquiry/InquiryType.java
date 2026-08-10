package kr.co.gymleco.domain.inquiry;

public enum InquiryType {
    QUOTE("견적"),
    DEMO("무료 시연"),
    OFFICIAL("오피셜 센터"),
    USED("중고"),
    PART("부품"),
    ETC("기타");
    private final String label;
    InquiryType(String label){
        this.label = label;
    }
    public String label(){
        return label;
    }
}
