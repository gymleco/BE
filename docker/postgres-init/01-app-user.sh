#!/bin/bash
# ══════════════════════════════════════════════════════════════
#  앱 전용 DB 계정 생성
#
#  앱이 슈퍼유저로 붙으면, SQL 인젝션이 하나라도 뚫렸을 때
#  DROP TABLE 까지 갈 수 있다. 최소 권한 원칙 (기획서 §12).
#
#  이 계정에 주지 않는 것:
#    - 슈퍼유저 / CREATEDB / CREATEROLE
#    - 다른 스키마 접근
#
#  주는 것: public 스키마의 DML + Flyway 가 쓰는 DDL
#  (Flyway 마이그레이션이 테이블을 만들어야 하므로 CREATE 는 필요하다)
# ══════════════════════════════════════════════════════════════
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '${APP_DB_USER}') THEN
            CREATE ROLE ${APP_DB_USER} LOGIN PASSWORD '${APP_DB_PASSWORD}'
                NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;
        END IF;
    END
    \$\$;

    GRANT CONNECT ON DATABASE ${POSTGRES_DB} TO ${APP_DB_USER};
    GRANT USAGE, CREATE ON SCHEMA public TO ${APP_DB_USER};

    -- 앞으로 만들어질 테이블·시퀀스에 대한 기본 권한
    ALTER DEFAULT PRIVILEGES IN SCHEMA public
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ${APP_DB_USER};
    ALTER DEFAULT PRIVILEGES IN SCHEMA public
        GRANT USAGE, SELECT ON SEQUENCES TO ${APP_DB_USER};

    -- 공개 접근을 막는다
    REVOKE ALL ON DATABASE ${POSTGRES_DB} FROM PUBLIC;
EOSQL

echo "앱 전용 계정 생성 완료: ${APP_DB_USER}"
