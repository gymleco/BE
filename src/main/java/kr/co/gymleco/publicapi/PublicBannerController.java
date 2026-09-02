package kr.co.gymleco.publicapi;

import kr.co.gymleco.domain.banner.BannerPosition;
import kr.co.gymleco.domain.banner.BannerRepository;
import kr.co.gymleco.domain.banner.SectionMedia;
import kr.co.gymleco.domain.banner.SectionMediaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicBannerController {

    private final BannerRepository banners;
    private final SectionMediaRepository sections;

    public PublicBannerController(BannerRepository banners,
                                  SectionMediaRepository sections) {
        this.banners = banners;
        this.sections = sections;
    }

    /** 기간 판정은 여기서 끝낸다 — 화면이 오늘 날짜를 따질 일이 없게 한다 */
    @GetMapping("/banners")
    @Transactional(readOnly = true)
    public Map<String, List<BannerResponse>> list(
        @RequestParam BannerPosition position) {
        return Map.of("items", banners.findLive(position, Instant.now())
            .stream().map(BannerResponse::from).toList());
    }

    /**
     * 메인이 한 번에 다 가져가게 맵으로 준다.
     * 구역마다 따로 부르면 왕복이 구역 수만큼 늘어난다.
     */
    @GetMapping("/section-media")
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> sectionMedia() {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        for (SectionMedia m : sections.findAll()) {
            out.put(m.getSectionKey(), Map.of(
                "pc", m.getImagePcKey(),
                "mobile", m.getImageMobileKey(),
                "alt", m.getAltText()));
        }
        return out;
    }
}
