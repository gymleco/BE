package kr.co.gymleco.publicapi;
import kr.co.gymleco.domain.used.UsedItem;
import kr.co.gymleco.domain.used.UsedItemRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/used")
public class PublicUsedController {
    private final UsedItemRepository repository;
    public PublicUsedController(UsedItemRepository repository){
        this.repository = repository;
    }
    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, List<PublicUsedItemResponse>> list() {
        return Map.of("items", repository.findVisible().stream()
            .map(PublicUsedItemResponse::summary)
            .toList());
    }
    @GetMapping("/{slug}")
    @Transactional(readOnly = true)
    public PublicUsedItemResponse detail(@PathVariable String slug) {
        UsedItem item = repository.findBySlugAndVisibleTrue(slug)
            .orElseThrow(() -> new UsedItemNotFoundException(slug));
        return PublicUsedItemResponse.detail(item);
    }
}
