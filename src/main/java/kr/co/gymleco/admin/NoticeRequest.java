package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record NoticeRequest(
    @NotBlank(message = "제목을 입력해 주세요.") @Size(max = 200) String title,
    @Size(max = 50000) String body,
    Boolean pinned,
    Instant publishedAt,
    Boolean visible
) {
}
