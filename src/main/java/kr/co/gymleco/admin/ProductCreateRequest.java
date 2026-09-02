package kr.co.gymleco.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
    @NotBlank(message = "주소(slug)를 입력해 주세요.")
    @Size(max = 120)
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "영문 소문자·숫자·하이픈만 사용할 수 있습니다.")
    String slug,
    @Valid ProductRequest product
) {
}
