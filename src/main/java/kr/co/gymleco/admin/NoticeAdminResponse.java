package kr.co.gymleco.admin;

/*
 * 관리 화면용 응답.
 *
 * 컨트롤러가 엔티티를 그대로 내보내면 두 가지가 따라온다.
 *  - 컬럼을 하나 추가하는 순간 API 로 자동 노출된다. 아무도 결정하지 않은 노출이다.
 *  - 연관관계가 하나라도 지연 로딩이면 open-in-view=false 라서
 *    직렬화 시점에 LazyInitializationException 이 난다.
 * 지금은 둘 다 해당이 없지만, 둘 다 "나중에 컬럼 하나 추가" 로 터진다.
 *
 * 공개용(PublicApi)과 따로 두는 이유는 보여줄 것이 다르기 때문이다.
 * 관리 화면은 visible·sortOrder 를 봐야 하고, 공개 API 는 보면 안 된다.
 */

import kr.co.gymleco.domain.support.Notice;

import java.time.Instant;

public record NoticeAdminResponse(
    Long id,
    String title,
    String body,
    boolean pinned,
    Instant publishedAt,
    boolean visible,
    Instant updatedAt
) {
    /** 목록에서는 본문을 싣지 않는다. 공지 50개면 본문만 수 MB 가 된다. */
    public static NoticeAdminResponse summary(Notice n) {
        return new NoticeAdminResponse(n.getId(), n.getTitle(), null, n.isPinned(),
            n.getPublishedAt(), n.isVisible(), n.getUpdatedAt());
    }
    public static NoticeAdminResponse detail(Notice n) {
        return new NoticeAdminResponse(n.getId(), n.getTitle(), n.getBody(), n.isPinned(),
            n.getPublishedAt(), n.isVisible(), n.getUpdatedAt());
    }
}
