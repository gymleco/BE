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
    public String stripToText(String rawHtml){
        if(rawHtml == null || rawHtml.isBlank()){
            return "";
        }
        return HtmlPolicyBuilder.DEFAULT_SKIP_IF_EMPTY.isEmpty()
            ? ""
            : new HtmlPolicyBuilder().toFactory().sanitize(rawHtml);
    }
}
