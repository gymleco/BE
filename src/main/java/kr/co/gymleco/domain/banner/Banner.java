package kr.co.gymleco.domain.banner;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * 페이지 히어로 배너.
 *
 * ── PC 와 모바일 이미지를 둘 다 받는 이유 ──
 *
 * 한 장으로 양쪽을 덮으면 반드시 한쪽이 깨진다. 가로로 넓은 사진을
 * 세로 화면에 넣으면 좌우가 잘려 기구가 프레임 밖으로 나가고,
 * 세로 사진을 데스크톱에 늘리면 화질이 무너진다.
 * "나중에 채우겠다" 로 비워 두면 그 상태로 배포된다.
 */
@Entity
@Table(name = "banner")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerPosition position;

    @Column(name = "image_pc_key", nullable = false, length = 255)
    private String imagePcKey;

    @Column(name = "image_mobile_key", nullable = false, length = 255)
    private String imageMobileKey;

    @Column(nullable = false, length = 120)
    private String title = "";

    @Column(nullable = false, length = 200)
    private String subtitle = "";

    @Column(name = "link_url", nullable = false, length = 255)
    private String linkUrl = "";

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false)
    private boolean visible = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Banner() {
    }

    public static Banner of(BannerPosition position, String pcKey, String mobileKey) {
        Banner b = new Banner();
        b.position = position;
        b.changeImages(pcKey, mobileKey);
        return b;
    }

    /**
     * ★ 여기서도 빈 값을 막는다.
     *
     *   DB 가 NOT NULL 로 막고 있지만, 제약에 걸리면 500 이 나가고
     *   관리 화면에는 "서버 오류" 만 뜬다. 여기서 막으면 400 과 함께
     *   무엇이 빠졌는지 알려줄 수 있다.
     */
    public void changeImages(String pcKey, String mobileKey) {
        if (pcKey == null || pcKey.isBlank()) {
            throw new IllegalArgumentException("PC 배경 이미지를 올려주세요.");
        }
        if (mobileKey == null || mobileKey.isBlank()) {
            throw new IllegalArgumentException("모바일 배경 이미지를 올려주세요.");
        }
        this.imagePcKey = pcKey;
        this.imageMobileKey = mobileKey;
        touch();
    }

    public void editText(String title, String subtitle, String linkUrl) {
        this.title = title == null ? "" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.linkUrl = linkUrl == null ? "" : linkUrl;
        touch();
    }

    public void changePeriod(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("종료일이 시작일보다 앞설 수 없습니다.");
        }
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        touch();
    }

    public void changePosition(BannerPosition position) { this.position = position; touch(); }
    public void changeVisibility(boolean visible)        { this.visible = visible; touch(); }
    public void changeSortOrder(int sortOrder)           { this.sortOrder = sortOrder; touch(); }

    private void touch() { this.updatedAt = Instant.now(); }

    public Long getId()                 { return id; }
    public BannerPosition getPosition() { return position; }
    public String getImagePcKey()       { return imagePcKey; }
    public String getImageMobileKey()   { return imageMobileKey; }
    public String getTitle()            { return title; }
    public String getSubtitle()         { return subtitle; }
    public String getLinkUrl()          { return linkUrl; }
    public Instant getStartsAt()        { return startsAt; }
    public Instant getEndsAt()          { return endsAt; }
    public int getSortOrder()           { return sortOrder; }
    public boolean isVisible()          { return visible; }
    public Instant getUpdatedAt()       { return updatedAt; }
}
