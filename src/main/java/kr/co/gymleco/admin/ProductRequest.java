package kr.co.gymleco.admin;

import jakarta.validation.constraints.*;
import kr.co.gymleco.domain.product.ProductCategory;
import kr.co.gymleco.domain.product.ProductType;

import java.math.BigDecimal;

public record ProductRequest(
    @NotNull ProductType type,
    @NotNull ProductCategory category,
    @NotBlank @Size(max = 120) String nameKo,
    @NotBlank @Size(max = 120) String nameEn,
    @Size(max = 200) String summary,
    @Size(max = 20000) String description,
    @Positive @Digits(integer = 4, fraction = 2) BigDecimal footprintM2,
    @Positive Integer widthMm,
    @Positive Integer depthMm,
    @Positive Integer heightMm,
    @Positive @Digits(integer = 5, fraction = 2) BigDecimal weightKg,
    @Size(max = 255) String thumbnailKey
    ) {
}
