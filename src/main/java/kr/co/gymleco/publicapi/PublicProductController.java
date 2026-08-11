package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.product.Product;
import kr.co.gymleco.domain.product.ProductCategory;
import kr.co.gymleco.domain.product.ProductRepository;
import kr.co.gymleco.domain.product.ProductType;
import kr.co.gymleco.service.admin.ProductNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/products")
public class PublicProductController {
    private final ProductRepository repository;
    public PublicProductController(ProductRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, List<PublicProductResponse>> list(
        @RequestParam(defaultValue = "EQUIPMENT") ProductType type,
        @RequestParam(required = false) ProductCategory category) {
        List<Product> products = (category == null)
            ? repository.findByVisibleTrueAndTypeOrderBySortOrderAscIdAsc(type)
            : repository.findByVisibleTrueAndTypeAndCategoryOrderBySortOrderAscIdAsc(type, category);
        return Map.of("items", products.stream()
            .map(PublicProductResponse::summary)
            .toList());
    }
    @GetMapping("/{slug}")
    @Transactional(readOnly = true)
    public PublicProductResponse detail(@PathVariable String slug) {
        Product product = repository.findBySlugAndVisibleTrue(slug)
            .orElseThrow(() -> new ProductNotFoundException(null));
        return PublicProductResponse.detail(product);
    }
}
