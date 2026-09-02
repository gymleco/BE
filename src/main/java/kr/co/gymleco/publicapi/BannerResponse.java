package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.banner.Banner;

/**
 * 공개 배너 응답.
 *
 * 노출 기간(startsAt·endsAt)과 정렬 순서를 담지 않는다.
 * 화면이 쓸 일이 없고, 운영 정보를 밖으로 내보낼 이유가 없다.
 * 기간 판정은 서버가 이미 끝낸 뒤 목록을 준다.
 */
public record BannerResponse(
    Long id,
    String imagePcKey,
    String imageMobileKey,
    String title,
    String subtitle,
    String linkUrl
) {
    public static BannerResponse from(Banner b) {
        return new BannerResponse(
            b.getId(),
            b.getImagePcKey(),
            b.getImageMobileKey(),
            b.getTitle(),
            b.getSubtitle(),
            b.getLinkUrl());
    }
}
