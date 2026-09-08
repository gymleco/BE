package kr.co.gymleco.publicapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequest (
    @NotBlank(message = "일렬번호를 입력해주세요.")
    @Size(max = 60, message = "일렬번호가 너무 깁니다.")
    String serial,
    @NotBlank(message = "모델번호를 입력해 주세요.")
    @Size(max = 40, message = "모델번호가 너무 깁니다.")
    String modelCode
){
}
