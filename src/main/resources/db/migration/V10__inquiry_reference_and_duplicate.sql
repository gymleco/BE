-- ═══════════════════════════════════════════════════════════════
-- V10 — 문의 접수번호 · 재문의 묶기
--
-- ── 왜 필요한가 ──
--
-- 문의는 사이트 폼으로 받고, 이어지는 대화는 카카오톡 채널에서 한다.
-- 두 곳이 «한 사람의 한 문의» 를 가리키려면 둘을 잇는 표식이 있어야 한다.
-- 그게 접수번호다. 방문자는 완료 화면에서 번호를 받아 카톡으로 보내고,
-- 담당자는 그 번호로 관리 화면의 문의를 찾는다.
--
-- ── 접수번호 형식 ──
--
--   GL-260911-K7Q2
--      ││││││ └┴┴┴ 무작위 4자 (0·O·1·I·L 제외 — 전화로 불러 줄 때 헷갈린다)
--      └┴┴┴┴┘ 접수일 (한국 시각)
--
-- ★ 일련번호로 만들지 않는다.
--   GL-260911-0023 이면 «오늘 문의가 23건» 이라는 영업 정보가 번호에
--   그대로 드러난다. 경쟁사가 문의 한 번만 넣어 보면 알 수 있다.
--
-- ★ 번호는 열쇠가 아니다.
--   번호로 문의 내용을 조회하는 공개 경로는 만들지 않는다. 번호만 알면
--   남의 문의를 볼 수 있게 되는 순간, 이 번호는 비밀번호가 되어 버린다.
--
-- ── 재문의 묶기 ──
--
-- 같은 연락처로 짧은 시간 안에 다시 접수되면(폼을 두 번 누르거나, 답이
-- 늦어 다시 넣는 경우) 담당자가 두 건을 서로 다른 고객으로 알고 두 번
-- 전화하게 된다.
--
-- ★ 두 번째 건을 버리지 않는다 — 묶는다.
--   버리면 두 번째에 적은 내용(다른 제품, 바뀐 평수)이 사라진다.
--   새 행으로 저장하되 duplicate_of_id 로 첫 건을 가리킨다.
--
-- ★ 두 번째 사람에게 첫 건의 번호를 돌려주지 않는다.
--   그러면 «이 전화번호가 최근에 문의했는가» 를 알아내는 통로가 된다.
--   남의 번호를 넣어 보면 그 사람의 접수번호가 튀어나오는 셈이다.
--   누구에게나 새 번호를 주고, 묶음은 관리 화면에서만 보인다.
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE inquiry
    ADD COLUMN reference_no    VARCHAR(20),
    ADD COLUMN duplicate_of_id BIGINT;

-- 기존 문의에도 번호를 채운다.
-- 무작위가 아니라 id 에서 만든다 — 마이그레이션 안에서는 충돌이 났을 때
-- 다시 뽑는 재시도를 할 수 없다. id 는 유일하므로 충돌할 수 없다.
-- 이 형식은 옛 문의에만 쓰이고, 새 문의는 앱이 무작위로 만든다.
--
-- ★ lpad 만 쓰면 안 된다.
--   PostgreSQL 의 lpad 는 문자열이 지정 길이보다 길면 «잘라 버린다».
--   id 가 0xFFFF 를 넘으면 뒷자리가 잘려 서로 다른 문의가 같은 번호를
--   받는다. greatest 로 길이를 늘려 잘리지 않게 한다.
UPDATE inquiry
   SET reference_no = 'GL-'
       || to_char(created_at AT TIME ZONE 'Asia/Seoul', 'YYMMDD')
       || '-'
       || lpad(upper(to_hex(id)), greatest(4, length(to_hex(id))), '0')
 WHERE reference_no IS NULL;

ALTER TABLE inquiry
    ALTER COLUMN reference_no SET NOT NULL,
    ADD CONSTRAINT uq_inquiry_reference_no UNIQUE (reference_no),
    -- 형식을 DB 가 강제한다. 앱이 틀린 번호를 만들어도 저장되지 않는다.
    -- 프론트의 lib/inquiry-reference.ts 와 같은 규칙이어야 한다.
    ADD CONSTRAINT ck_inquiry_reference_no
        CHECK (reference_no ~ '^GL-[0-9]{6}-[0-9A-Z]{4,8}$'),
    -- 첫 건이 보유기간이 지나 파기되면 묶음 표시만 사라지고
    -- 두 번째 건은 남는다. 두 번째 건에도 자기 보유기간이 있다.
    ADD CONSTRAINT fk_inquiry_duplicate_of
        FOREIGN KEY (duplicate_of_id) REFERENCES inquiry (id) ON DELETE SET NULL,
    ADD CONSTRAINT ck_inquiry_not_own_duplicate
        CHECK (duplicate_of_id IS NULL OR duplicate_of_id <> id);

-- 재문의 확인은 «이 연락처로 최근 N시간 안에 들어온 문의» 를 찾는다.
-- 문의가 쌓일수록 느려지는 조회라 처음부터 인덱스를 건다.
CREATE INDEX ix_inquiry_phone_recent
    ON inquiry (phone_blind_index, created_at DESC);

-- V1 의 단일 컬럼 인덱스는 이제 필요 없다.
-- 복합 인덱스의 앞 컬럼이 같으므로 «이 번호의 문의 전부» 조회
-- (관리 화면의 연락처 검색)도 새 인덱스가 그대로 받는다.
-- 둘 다 두면 문의가 들어올 때마다 인덱스를 두 번 쓴다.
DROP INDEX IF EXISTS ix_inquiry_phone_blind;

COMMENT ON COLUMN inquiry.reference_no IS
    '접수번호. 사이트 문의와 카카오톡 대화를 잇는 표식. 조회 열쇠로 쓰지 않는다.';
COMMENT ON COLUMN inquiry.duplicate_of_id IS
    '같은 연락처로 짧은 시간 안에 다시 들어온 문의면 첫 건의 id. 관리 화면에서만 쓴다.';
