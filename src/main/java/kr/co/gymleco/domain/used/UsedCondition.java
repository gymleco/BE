package kr.co.gymleco.domain.used;

public enum UsedCondition {
    A("상급", "사용감이 거의 없습니다."),
    B("중급", "사용감은 있으나 작동에 문제가 없습니다."),
    C("하급", "외관 손상이 있습니다. 정비 후 출고됩니다.");
    private final String label;
    private final String note;
    UsedCondition(String label, String note){
        this.label = label;
        this.note = note;
    }
    public String label(){return label;}
    public String note(){ return note;}
}
