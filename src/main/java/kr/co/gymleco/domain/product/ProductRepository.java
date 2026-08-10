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
}
