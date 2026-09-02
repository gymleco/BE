package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.gymleco.domain.banner.BannerPosition;

import java.time.Instant;

public record BannerRequest (
    @NotNull(message = "배너 위치를 선택해 주세요.") BannerPosition position,
    @NotBlank(message = "PC배경 이미지를 올려주세요.")
    @Size(max = 255) String imagePcKey,
    @NotBlank(message = "모바일 배경 이미지를 올려주세요.")
    @Size(max = 255) String imageMobileKey,
    @Size(max = 120) String title,
    @Size(max = 200) String subtitle,
    @Size(max = 255) String linkUrl,
    Instant startsAt,
    Instant endsAt,
    Integer sortOrder,
    Boolean visible
    ){
}
