package kr.co.gymleco.support.html;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {
    private final PolicyFactory policy;

    public HtmlSanitizer() {
        this.policy = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(new HtmlPolicyBuilder()
                .allowElements("a")
                .allowAttributes("target").matching(java.util.regex.Pattern.compile("_blank"))
                .onElements("a")
                .requireRelsOnLinks("noopener", "noreferrer")
                .toFactory());
    }
    public String sanitize(String rawHtml){
        if(rawHtml == null || rawHtml.isBlank()){
            return "";
        }
        return policy.sanitize(rawHtml);
    }
    /**
     * 태그를 전부 제거하고 텍스트만 남긴다.
     * 요약문·메타 설명처럼 서식이 필요 없는 곳에 쓴다.
     *
     * 빈 정책은 "허용 요소 없음" 이므로 모든 태그가 제거되고 텍스트만 남는다.
     * 정책 객체는 스레드 안전하고 재사용 가능하므로 호출마다 만들지 않는다.
     */
    private static final PolicyFactory TEXT_ONLY = new HtmlPolicyBuilder().toFactory();

    public String stripToText(String rawHtml) {
        if (rawHtml == null || rawHtml.isBlank()) {
            return "";
        }
        return TEXT_ONLY.sanitize(rawHtml);
    }
}
