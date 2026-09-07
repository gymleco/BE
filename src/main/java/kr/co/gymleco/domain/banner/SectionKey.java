package kr.co.gymleco.domain.banner;

import java.util.Arrays;
import java.util.Optional;

public enum SectionKey {

    HOME_WHY("home.why", "「왜 짐레코인가」 구역"),
    HOME_CTA("home.cta", "맨 아래 문의 유도 구역"),
    HOME_STATEMENT("home.statement", "「좋은 기구는 조용합니다」 구역");

    private final String key;
    private final String label;

    SectionKey(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String key()   { return key; }
    public String label() { return label; }

    public static Optional<SectionKey> of(String key) {
        return Arrays.stream(values()).filter(k -> k.key.equals(key)).findFirst();
    }
}
