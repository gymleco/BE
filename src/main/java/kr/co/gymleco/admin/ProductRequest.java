package kr.co.gymleco.admin;

import jakarta.validation.constraints.*;
import kr.co.gymleco.domain.product.ProductCategory;
import kr.co.gymleco.domain.product.ProductType;

import java.math.BigDecimal;

public record ProductRequest(
    @NotNull(message = "제품 종류를 선택해 주세요.") ProductType type,
    @NotNull(message = "분류를 선택해 주세요.") ProductCategory category,
    @NotBlank(message = "제품명(한글)을 입력해 주세요.") @Size(max = 120) String nameKo,
    @NotBlank(message = "제품명(영문)을 입력해 주세요.") @Size(max = 120) String nameEn,
    @Size(max = 200) String summary,
    @Size(max = 20000) String description,
    @Positive @Digits(integer = 4, fraction = 2) BigDecimal footprintM2,
    @Positive Integer widthMm,
    @Positive Integer depthMm,
    @Positive Integer heightMm,
    @Positive @Digits(integer = 5, fraction = 2) BigDecimal weightKg,
    @Size(max = 255) String thumbnailKey,
    @Size(max = 255) String cutoutKey
    ) {
}
