-- ═══════════════════════════════════════════════════════════════
--  V5 — IP 컬럼을 INET → VARCHAR(45)
--
--  ── 왜 바꾸는가 ──
--
--  INET 은 PostgreSQL 고유 타입이라 varchar 파라미터와 직접 비교되지 않는다.
--
--      ERROR: operator does not exist: inet = character varying
--
--  JPA 는 String 필드를 varchar 로 바인딩하므로, INET 컬럼을 쓰려면
--  조회·삽입 모든 경로에 명시적 캐스팅(::inet)이 필요하다.
--  네이티브 쿼리로 내려가거나 커스텀 타입을 등록해야 하는데,
--  그 대가로 얻는 것은 "DB 가 IP 형식을 검증해 준다" 정도다.
--
--  IP 는 요청에서 받아 서버가 채우는 값이라 사용자가 임의로 넣을 수 없고,
--  형식 검증의 값어치가 캐스팅 비용을 넘지 않는다.
--
--  실제로 문의 접수의 rate limit 조회에서 500 이 났고, INSERT 에서도
--  같은 문제가 재현될 상황이었다.
--
--  VARCHAR(45) 는 IPv6 최대 표기 길이
--  (0000:0000:0000:0000:0000:ffff:255.255.255.255 = 45자) 기준이다.
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE inquiry
    ALTER COLUMN source_ip TYPE VARCHAR(45) USING source_ip::text;

ALTER TABLE admin_user
    ALTER COLUMN last_login_ip TYPE VARCHAR(45) USING last_login_ip::text;

ALTER TABLE admin_refresh_token
    ALTER COLUMN ip TYPE VARCHAR(45) USING ip::text;

ALTER TABLE login_attempt
    ALTER COLUMN ip TYPE VARCHAR(45) USING ip::text;

ALTER TABLE admin_audit_log
    ALTER COLUMN ip TYPE VARCHAR(45) USING ip::text;

-- login_attempt 의 IP 인덱스는 타입 변경으로 재생성된다.
-- 명시적으로 다시 만들어 의도를 남긴다.
DROP INDEX IF EXISTS ix_login_attempt_ip;
CREATE INDEX ix_login_attempt_ip ON login_attempt (ip, attempted_at DESC);

COMMENT ON COLUMN inquiry.source_ip IS
    '스팸 분석·rate limit 용. 개인정보이므로 inquiry 행과 함께 파기된다.';
