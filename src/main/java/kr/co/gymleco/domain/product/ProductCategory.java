package kr.co.gymleco.domain.product;

public enum ProductCategory {
    STRENGTH("머신"),
    CABLE("케이블"),
    RACK("랙"),
    BENCH("벤치"),
    CARDIO("유산소"),
    PART("부품"),
    ACCESSORY("악세사리");
    private final String label;
    ProductCategory(String label){
        this.label = label;
    }
    public String label(){
        return label;
    }
}
