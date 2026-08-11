package kr.co.gymleco.domain.product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVisibleTrueAndTypeOrderBySortOrderAscIdAsc(ProductType type);
    List<Product> findByVisibleTrueAndTypeAndCategoryOrderBySortOrderAscIdAsc(
        ProductType type, ProductCategory category);
    @EntityGraph(attributePaths = "images")
    Optional<Product> findBySlugAndVisibleTrue(String slug);
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    /*
     * 관리자 목록 — 비공개 포함.
     *
     * findAll() 후 메모리에서 거르지 않는다. 제품·부품·악세사리가
     * 한 테이블에 있어 전체를 끌어오면 종류 하나 보려고 나머지를
     * 전부 로드하게 된다.
     */
    List<Product> findAllByOrderBySortOrderAscIdAsc();

    List<Product> findByTypeOrderBySortOrderAscIdAsc(ProductType type);
    List<Product> findBySlugInAndVisibleTrue(java.util.Collection<String> slugs);
}
