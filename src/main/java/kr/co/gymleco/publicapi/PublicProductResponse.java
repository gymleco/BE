package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.product.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * 공개 응답.
 *
 * ★ 관리자 응답(ProductAdminResponse)과 분리한다.
 *
 * 하나로 합치면 sortOrder·visible·updatedAt 같은 내부 값이 공개 API 로
 * 새어 나간다. 지금은 무해해 보이지만, 나중에 원가나 재고 같은 필드가
 * 엔티티에 붙는 순간 그대로 노출된다.
 * "무엇을 감출까" 가 아니라 "무엇을 보여줄까" 로 시작해야 한다.
 */
public record PublicProductResponse(
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
    List<String> imageKeys
) {
    /** 목록용 — 설명과 갤러리를 뺀다. 목록에서 쓰지 않는 데이터를 보내지 않는다. */
    public static PublicProductResponse summary(Product p) {
        return new PublicProductResponse(
            p.getSlug(), p.getType().name(), p.getCategory().name(),
            p.getNameKo(), p.getNameEn(), p.getSummary(), null,
            p.getFootprintM2(), p.getWidthMm(), p.getDepthMm(), p.getHeightMm(),
            p.getWeightKg(), p.getThumbnailKey(),p.getCutoutKey(), List.of());
    }

    public static PublicProductResponse detail(Product p) {
        return new PublicProductResponse(
            p.getSlug(), p.getType().name(), p.getCategory().name(),
            p.getNameKo(), p.getNameEn(), p.getSummary(), p.getDescription(),
            p.getFootprintM2(), p.getWidthMm(), p.getDepthMm(), p.getHeightMm(),
            p.getWeightKg(), p.getThumbnailKey(), p.getCutoutKey(),
            p.getImages().stream().map(i -> i.getImageKey()).toList());
    }
}
