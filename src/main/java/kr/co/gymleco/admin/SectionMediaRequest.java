package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionMediaRequest(
    @NotBlank(message = "PC 이미지를 올려주세요.")
    @Size(max = 255) String imagePcKey,
    @NotBlank(message = "모바일 이미지를 올려주세요.")
    @Size(max = 255) String imageMobileKey,
    @Size(max = 200) String altText
) {
}
