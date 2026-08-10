package kr.co.gymleco.infra.revalidate;

import java.util.List;
public record ContentChangedEvent(List<String> paths) {
    public static ContentChangedEvent product(String slug) {
        return new ContentChangedEvent(List.of("/", "/products", "/products/" + slug));
    }

    public static ContentChangedEvent news(String slug) {
        return new ContentChangedEvent(List.of("/", "/news", "/news/" + slug));
    }

    public static ContentChangedEvent settings() {
        return new ContentChangedEvent(List.of("/"));
    }
}
