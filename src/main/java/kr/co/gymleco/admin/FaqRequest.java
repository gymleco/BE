package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FaqRequest(
    @Size(max = 40) String category,
    @NotBlank(message = "질문을 입력해 주세요.") @Size(max = 200) String question,
    @Size(max = 20000) String answer,
    Integer sortOrder,
    Boolean visible
) {
}
