package kr.co.gymleco.admin;

import jakarta.validation.constraints.NotNull;
import kr.co.gymleco.domain.used.UsedStatus;

public record UsedStatusRequest(@NotNull(message = "상태를 선택해 주세요.") UsedStatus status) {
}
