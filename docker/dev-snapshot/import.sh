#!/usr/bin/env bash
#
# 개발용 스냅숏 들여오기.
#
#   1) 저장소를 받고  2) docker compose up -d  로 db·minio 가 뜬 뒤
#   3) bash docker/dev-snapshot/import.sh [스냅숏 폴더]
#
# ★ 마이그레이션보다 먼저 돌리지 않는다.
#   pg_dump 는 스키마까지 들고 있으므로 순서가 뒤바뀌면 Flyway 이력이
#   어긋난다. api 가 한 번 떠서 V1~V9 를 적용한 뒤에 넣는다.
#
# ★ 운영 서버에서 돌리지 않는다.
#   --clean 이 기존 표를 지우고 덮어쓴다.
#
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
BE="$(cd "$HERE/../.." && pwd)"
SNAP="${1:-$BE/dev-snapshot}"

cd "$BE"

BUCKET="${S3_BUCKET:-gymleco-images}"
DB="${POSTGRES_DB:-gymleco}"
USER="${POSTGRES_SUPERUSER:-postgres}"

[ -f "$SNAP/db.sql" ] || { echo "db.sql 이 없습니다: $SNAP"; exit 1; }

echo "── 데이터베이스 ──"
docker compose exec -T db psql -U "$USER" -d "$DB" -q < "$SNAP/db.sql" > /dev/null
docker compose exec -T db psql -U "$USER" -d "$DB" -Atc \
  "select '  제품 ' || count(*) from product"

echo "── 이미지 ──"
docker compose exec -T minio sh -c "
  mc alias set local http://localhost:9000 \
     '${S3_ACCESS_KEY:-gymleco-local}' '${S3_SECRET_KEY:-gymleco-local-secret}' >/dev/null 2>&1
  mc mb --ignore-existing local/$BUCKET >/dev/null 2>&1
  rm -rf /tmp/snap && mkdir -p /tmp/snap
"
docker compose cp "$SNAP/images/." minio:/tmp/snap/ >/dev/null
docker compose exec -T minio sh -c "
  mc mirror --quiet --overwrite /tmp/snap local/$BUCKET >/dev/null 2>&1
  # 공개 읽기 — 브라우저가 CDN 대신 여기서 직접 받는다
  mc anonymous set download local/$BUCKET >/dev/null 2>&1
  echo \"  \$(mc ls -r local/$BUCKET | wc -l) 개\"
"

echo
echo "완료. api 를 다시 띄우면(docker compose restart api) 화면에 반영됩니다."
