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

import kr.co.gymleco.domain.banner.SectionMedia;

import java.time.Instant;

public record SectionMediaAdminResponse(
    String sectionKey,
    String imagePcKey,
    String imageMobileKey,
    String altText,
    Instant updatedAt
) {
    public static SectionMediaAdminResponse from(SectionMedia m) {
        return new SectionMediaAdminResponse(m.getSectionKey(), m.getImagePcKey(),
            m.getImageMobileKey(), m.getAltText(), m.getUpdatedAt());
    }
}
