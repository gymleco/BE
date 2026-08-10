package kr.co.gymleco.domain.product;

public enum ProductType {
    EQUIPMENT("기구"),
    PART("부품"),
    ACCESSORY("악세사리");
    private final String label;
    ProductType(String label){
        this.label = label;
    }
    public String label(){
        return label;
    }
    public boolean requiresFootprint(){
        return this == EQUIPMENT;
    }
}
