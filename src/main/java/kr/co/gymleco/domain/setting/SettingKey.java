package kr.co.gymleco.domain.setting;

import java.util.Arrays;
import java.util.Optional;

/**
 * 사이트 설정에서 쓸 수 있는 키.
 *
 * ★ 키를 자유 문자열로 두지 않는 이유
 *   관리자가 아무 키나 만들 수 있으면, 화면은 아무도 정의하지 않은 키를
 *   읽게 되고 오타로 만든 키가 조용히 쌓인다. 여기 없는 키는 거부한다.
 *
 * ★ 값의 «종류» 를 함께 두는 이유
 *   sns.instagram 은 주소이고 contact.phone 은 전화번호다. 종류를 모르면
 *   주소 칸에 javascript: 로 시작하는 값이 들어가고, 화면이 <a href> 로
 *   그리는 순간 그게 XSS 가 된다. 검증할 근거를 키가 직접 들고 있어야 한다.
 *
 * ★ 공개 여부
 *   공개 사이트가 읽어도 되는 값만 public 으로 둔다. hero_product_slugs 처럼
 *   운영에만 쓰는 값은 굳이 밖으로 내보내지 않는다.
 */
public enum SettingKey {

    // ── 사업자 (푸터 표시 의무) ──
    COMPANY_NAME("company.name", "상호", SettingType.TEXT, 120, true),
    COMPANY_CEO("company.ceo", "대표자", SettingType.TEXT, 60, true),
    COMPANY_REGISTRATION_NO("company.registration_no", "사업자등록번호",
        SettingType.TEXT, 40, true),

    // ── 연락처 ──
    CONTACT_PHONE("contact.phone", "대표 전화", SettingType.PHONE, 40, true),
    CONTACT_EMAIL("contact.email", "대표 이메일", SettingType.EMAIL, 160, true),
    CONTACT_ADDRESS("contact.address", "주소", SettingType.TEXT, 200, true),
    CONTACT_BUSINESS_HOURS("contact.business_hours", "영업시간",
        SettingType.TEXT, 120, true),

    // ── SNS ──
    SNS_INSTAGRAM("sns.instagram", "인스타그램 주소", SettingType.URL, 255, true),
    SNS_YOUTUBE("sns.youtube", "유튜브 주소", SettingType.URL, 255, true),
    SNS_BLOG("sns.blog", "블로그 주소", SettingType.URL, 255, true),

    // ── 메인 화면 ──
    HOME_HERO_PRODUCT_SLUGS("home.hero_product_slugs",
        "첫 화면 원판에 올릴 제품", SettingType.SLUG_LIST, 500, false),
    HOME_BANNER_TEXT("home.banner_text", "상단 띠 문구", SettingType.TEXT, 200, true),
    HOME_BANNER_LINK("home.banner_link", "상단 띠 링크", SettingType.URL, 255, true),
    HOME_SECTION_WHY_TITLE("home.section_why_title", "「왜 짐레코인가」 제목",
        SettingType.TEXT, 120, true),

    // ── 고객센터 ──
    SUPPORT_FAQ_INTRO("support.faq_intro", "FAQ 안내 문구",
        SettingType.TEXT, 300, true),
    SUPPORT_NOTICE_INTRO("support.notice_intro", "공지 안내 문구",
        SettingType.TEXT, 300, true),

    // ── 개인정보 ──
    PRIVACY_RETENTION_NOTICE("privacy.retention_notice", "문의 보관 안내",
        SettingType.TEXT, 300, true),

    // ── 화면 ──
    THEME_DEFAULT("theme.default", "기본 테마", SettingType.THEME, 10, true);

    private final String key;
    private final String label;
    private final SettingType type;
    private final int maxLength;
    private final boolean publicValue;

    SettingKey(String key, String label, SettingType type,
               int maxLength, boolean publicValue) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.maxLength = maxLength;
        this.publicValue = publicValue;
    }

    public String key()        { return key; }
    public String label()      { return label; }
    public SettingType type()  { return type; }
    public int maxLength()     { return maxLength; }
    public boolean isPublic()  { return publicValue; }

    /** DB 의 key 문자열로 찾는다. 모르는 키면 비어 있다. */
    public static Optional<SettingKey> of(String key) {
        return Arrays.stream(values())
            .filter(k -> k.key.equals(key))
            .findFirst();
    }
}
