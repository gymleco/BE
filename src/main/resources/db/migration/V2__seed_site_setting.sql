-- ═══════════════════════════════════════════════════════════════
--  V2 — 사이트 설정 기본값
--
--  관리 화면에서 대표님이 직접 수정하는 값들이다.
--  코드에 하드코딩하면 수정할 때마다 개발자를 불러야 한다 (기획서 §4.1).
--
--  실제 값은 Phase 0 에서 대표님께 받아 관리 화면으로 입력한다.
--  여기서는 키만 만들어 둔다 — 키가 없으면 화면에 입력란이 뜨지 않는다.
-- ═══════════════════════════════════════════════════════════════

INSERT INTO site_setting (key, value) VALUES
    ('contact.phone',            ''),
    ('contact.email',            ''),
    ('contact.address',          ''),
    ('contact.business_hours',   ''),

    ('company.name',             '짐레코 코리아'),
    ('company.ceo',              ''),
    ('company.registration_no',  ''),

    ('sns.instagram',            ''),
    ('sns.youtube',              ''),
    ('sns.blog',                 ''),

    -- 메인 히어로에 노출할 제품 slug 목록 (쉼표 구분, 순서가 곧 노출 순서)
    ('home.hero_product_slugs',  ''),
    -- 메인 상단 공지 배너. 비어 있으면 배너를 렌더하지 않는다.
    ('home.banner_text',         ''),
    ('home.banner_link',         ''),

    -- 개인정보 보유기간 안내 문구 (처리방침과 문의 폼에 함께 노출)
    ('privacy.retention_notice', '문의 처리 완료 후 1년간 보관 후 파기합니다.')
ON CONFLICT (key) DO NOTHING;
