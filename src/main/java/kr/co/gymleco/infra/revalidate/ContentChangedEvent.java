package kr.co.gymleco.infra.revalidate;

import java.util.List;

/**
 * 무엇이 바뀌었는지 프론트에 알린다.
 *
 * ★ 경로만으로는 부족하다
 *   사이트 설정(상호·전화·SNS)은 푸터에 들어가므로 «모든» 페이지에 걸린다.
 *   경로를 하나하나 적을 수 없고, 그렇다고 매번 전체를 갱신하면 제품 하나
 *   고칠 때마다 사이트 전체가 다시 만들어진다. 그래서 태그도 함께 보낸다 —
 *   무엇이 바뀌었는지는 서버가 가장 잘 안다.
 */
public record ContentChangedEvent(List<String> paths, List<String> tags) {

    /** 태그가 필요 없는 대부분의 경우 — 기존 호출부가 그대로 동작한다. */
    public ContentChangedEvent(List<String> paths) {
        this(paths, List.of());
    }

    public static ContentChangedEvent product(String slug) {
        return new ContentChangedEvent(List.of("/", "/products", "/products/" + slug));
    }

    public static ContentChangedEvent news(String slug) {
        return new ContentChangedEvent(List.of("/", "/news", "/news/" + slug));
    }

    public static ContentChangedEvent settings() {
        return new ContentChangedEvent(List.of("/"), List.of("settings"));
    }
}
