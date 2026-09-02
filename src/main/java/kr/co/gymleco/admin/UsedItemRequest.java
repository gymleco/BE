package kr.co.gymleco.admin;

import jakarta.validation.constraints.*;
import kr.co.gymleco.domain.used.UsedCondition;

public record UsedItemRequest (
    @NotBlank(message = "제품명을 입력해 주세요.") @Size(max = 140) String nameKo,
    @Size(max= 140) String modelName,
    @NotNull(message = "상태 등급을 선택해 주세요.") UsedCondition conditionGrade,
    @Min(1980) @Max(2100) Short yearMade,
    @Positive Integer priceKrw,
    @Size(max = 20000) String description,
    @Size(max = 255) String thumbnailKey,
    @Min(0) @Max(999) Short quantity,
    Long productId
){
}
