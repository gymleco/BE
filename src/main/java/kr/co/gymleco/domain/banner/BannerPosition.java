package kr.co.gymleco.domain.banner;

public enum BannerPosition {
    MAIN("메인"),
    PRODUCT("제품"),
    USED("중고"),
    PART("부품"),
    ACCESSORY("악세사리"),
    CENTER("공식 헬스장");

    private final String label;

    BannerPosition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
