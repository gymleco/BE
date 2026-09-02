package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.support.Notice;

import java.time.Instant;

public record NoticeResponse(Long id, String title, String body, boolean pinned, Instant publishedAt) {
    public static NoticeResponse summary(Notice n) {
        return new NoticeResponse(n.getId(), n.getTitle(), null, n.isPinned(), n.getPublishedAt());
    }
    public static NoticeResponse detail(Notice n) {
        return new NoticeResponse(n.getId(), n.getTitle(), n.getBody(), n.isPinned(), n.getPublishedAt());
    }
}
