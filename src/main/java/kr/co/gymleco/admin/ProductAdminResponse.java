package kr.co.gymleco.admin;

import kr.co.gymleco.domain.product.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductAdminResponse(
    Long id,
    String slug,
    String type,
    String category,
    String nameKo,
    String nameEn,
    String summary,
    String description,
    BigDecimal footprintM2,
    Integer widthMm,
    Integer depthMm,
    Integer heightMm,
    BigDecimal weightKg,
    String thumbnailKey,
    String cutoutKey,
    int sortOrder,
    boolean visible,
    List<String> imageKeys,
    Instant updatedAt
) {
    public static ProductAdminResponse from(Product p) {
        return new ProductAdminResponse(
            p.getId(), p.getSlug(), p.getType().name(), p.getCategory().name(),
            p.getNameKo(), p.getNameEn(), p.getSummary(), p.getDescription(),
            p.getFootprintM2(), p.getWidthMm(), p.getDepthMm(), p.getHeightMm(),
            p.getWeightKg(), p.getThumbnailKey(),p.getCutoutKey(), p.getSortOrder(), p.isVisible(),
            p.getImages().stream().map(i -> i.getImageKey()).toList(),
            p.getUpdatedAt());
    }
}
