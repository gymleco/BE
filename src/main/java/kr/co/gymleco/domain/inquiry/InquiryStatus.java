package kr.co.gymleco.domain.inquiry;

public enum InquiryStatus {
    NEW("신규"),
    CONTACTING("연락중"),
    DONE("완료"),
    SPAM("스팸");
    private final String label;
    InquiryStatus(String label){
        this.label = label;
    }
    public String label(){
        return label;
    }
}
