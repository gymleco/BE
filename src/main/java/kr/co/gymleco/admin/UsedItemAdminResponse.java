package kr.co.gymleco.admin;
import kr.co.gymleco.domain.used.UsedItem;
import java.time.Instant;
import java.util.List;

public record UsedItemAdminResponse(
    Long id,
    String slug,
    String nameKo,
    String modelName,
    String conditionGrade,
    Short yearMade,
    Integer priceKrw,
    String description,
    String thubnailKey,
    String status,
    short quantity,
    int sortOrder,
    boolean visible,
    Long productId,
    List<String> imageKeys,
    Instant updatedAt
) {
    public static UsedItemAdminResponse from(UsedItem u){
        return new UsedItemAdminResponse(
            u.getId(), u.getSlug(), u.getNameKo(), u.getModelName(),
            u.getConditionGrade().name(), u.getYearMade(), u.getPriceKrw(),
            u.getDescription(), u.getThumbnailKey(),
            u.getStatus().name(), u.getQuantity(), u.getSortOrder(),
            u.isVisible(),
            u.getProduct() == null ? null : u.getProduct().getId(),
            u.getImages().stream().map(i -> i.getImageKey()).toList(),
            u.getUpdatedAt()
        );
    }
}
