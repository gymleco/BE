package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.support.Faq;

public record FaqResponse(Long id, String category, String question, String answer) {
    public static FaqResponse from(Faq f){
        return new FaqResponse(f.getId(), f.getCategory(), f.getQuestion(), f.getAnswer());
    }
}
