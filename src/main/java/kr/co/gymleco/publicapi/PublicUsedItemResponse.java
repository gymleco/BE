package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.used.UsedItem;
import kr.co.gymleco.domain.used.UsedItemImage;

import java.util.List;
public record PublicUsedItemResponse(
    String slug,
    String nameKo,
    String modelName,
    String conditionGrade,
    String conditionLabel,
    String conditionNote,
    Short yearMade,
    Integer priceKrw,
    String priceLabel,
    String status,
    String statusLabel,
    String description,
    String thumbnailKey,
    List<Image> images
) {
    public record Image(String key, String alt) {}

    public static PublicUsedItemResponse summary(UsedItem item) {
        return of(item, List.of());
    }

    public static PublicUsedItemResponse detail(UsedItem item) {
        return of(item, item.getImages().stream()
            .map(PublicUsedItemResponse::toImage)
            .toList());
    }

    private static Image toImage(UsedItemImage image) {
        return new Image(image.getImageKey(), image.getAltText());
    }

    private static PublicUsedItemResponse of(UsedItem item, List<Image> images) {
        return new PublicUsedItemResponse(
            item.getSlug(),
            item.getNameKo(),
            item.getModelName(),
            item.getConditionGrade().name(),
            item.getConditionGrade().label(),
            item.getConditionGrade().note(),
            item.getYearMade(),
            item.getPriceKrw(),
            item.getPriceKrw() == null ? "가격 문의" : null,
            item.getStatus().name(),
            item.getStatus().label(),
            item.getDescription(),
            item.getThumbnailKey(),
            images);
    }
}
