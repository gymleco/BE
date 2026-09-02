package kr.co.gymleco.domain.support;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "faq")
public class Faq {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 40)
    private String category = "일반";
    @Column(nullable = false, length = 200)
    private String question;
    @Column(nullable = false, columnDefinition = "text")
    private String answer = "";
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    @Column(nullable =false)
    private boolean visible =true;
    @Column(name = "created_at", nullable = false, updatable =false)
    private Instant createdAt = Instant.now();
    // updatable = false 를 두면 안 된다. touch() 가 자바 필드만 바꾸고
    // Hibernate 는 이 컬럼을 UPDATE 문에 넣지 않아, 수정해도 DB 의
    // updated_at 이 영영 생성 시각에 머문다. createdAt 은 반대로 그게 맞다.
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected Faq(){}
    public static Faq of(String category, String question){
        Faq f = new Faq();
        f.category = (category == null || category.isBlank()) ? "일반" : category;
        f.question = question;
        return f;
    }
    public void edit(String category, String question, String answer){
        if(category != null && !category.isBlank()) this.category = category;
        this.question = question;
        this.answer = answer == null ? "" : answer;
        touch();
    }
    public void changeVisibility(boolean visible){this.visible = visible; touch();}
    public void changeSortOrder(int sortOrder){this.sortOrder = sortOrder; touch();}
    private void touch(){ this.updatedAt = Instant.now();}
    public Long getId(){return id;}
    public String getCategory(){return category;}
    public String getQuestion(){return question;}
    public String getAnswer(){return answer;}
    public int getSortOrder(){return sortOrder;}
    public boolean isVisible(){return visible;}
    public Instant getUpdatedAt(){return updatedAt;}
}
