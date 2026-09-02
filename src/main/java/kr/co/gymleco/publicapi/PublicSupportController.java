package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.support.FaqRepository;
import kr.co.gymleco.domain.support.NoticeRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/support")
public class PublicSupportController {
    private final FaqRepository faqs;
    private final NoticeRepository notices;
    public PublicSupportController(FaqRepository faqs, NoticeRepository notices) {
        this.faqs = faqs;
        this.notices = notices;
    }
    @GetMapping("/faq") @Transactional(readOnly = true) public Map<String, List<FaqResponse>> faq() {return Map.of("items", faqs.findByVisibleTrueOrderByCategoryAscSortOrderAscIdAsc().stream().map(FaqResponse::from).toList());}
    @GetMapping("/notice") @Transactional(readOnly = true) public Map<String, List<NoticeResponse>> notices() {return Map.of("items", this.notices.findPublished().stream().map(NoticeResponse::summary).toList());}
    @GetMapping("/notice/{id}") @Transactional(readOnly = true) public NoticeResponse notice(@PathVariable Long id) {return this.notices.findByIdAndVisibleTrue(id).map(NoticeResponse::detail).orElseThrow(() -> new UsedItemNotFoundException("notice-" + id));}
}
