package kr.co.gymleco.domain.used;

public enum UsedStatus {
    AVAILABLE("판매중"),
    RESERVED("예약중"),
    SOLD("판매완료");

    private final String label;

    UsedStatus(String label) {
        this.label = label;
    }

    public String label() { return label; }
}
