#!/usr/bin/env bash
#
# 개발용 스냅숏 내보내기 — 다른 PC 에서 같은 화면을 띄우기 위한 것.
#
# ── 왜 필요한가 ──
#
# 제품·사진은 마이그레이션에 없다. 제품은 관리 화면에서 넣는 «내용» 이고,
# 사진은 오브젝트 스토리지에 있다. 둘 다 도커 볼륨 안이라 저장소를 받아도
# 따라오지 않는다. 새 PC 에서 그냥 띄우면 제품 0건 → 자리표시자로 폴백해
# 「파워 랙」 에 노란 면적 타일이 도는 옛 화면이 나온다.
#
# ── 운영 데이터를 옮기는 도구가 아니다 ──
#
# 시연용 자료를 옮기기 위한 것이다. 운영 DB 를 이걸로 복사하지 않는다 —
# 문의(개인정보)까지 통째로 따라간다.
#
#   사용:  bash docker/dev-snapshot/export.sh [내보낼 폴더]
#
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
BE="$(cd "$HERE/../.." && pwd)"
OUT="${1:-$BE/dev-snapshot}"

cd "$BE"

BUCKET="${S3_BUCKET:-gymleco-images}"
DB="${POSTGRES_DB:-gymleco}"
USER="${POSTGRES_SUPERUSER:-postgres}"

mkdir -p "$OUT/images"

echo "── 데이터베이스 ──"
# --clean --if-exists: 받는 쪽에 이미 있어도 덮어쓴다
docker compose exec -T db pg_dump -U "$USER" -d "$DB" --clean --if-exists > "$OUT/db.sql"
echo "  db.sql  $(wc -c < "$OUT/db.sql" | tr -d ' ') 바이트"

echo "── 이미지 ──"
docker compose exec -T minio sh -c "
  mc alias set local http://localhost:9000 \
     '${S3_ACCESS_KEY:-gymleco-local}' '${S3_SECRET_KEY:-gymleco-local-secret}' >/dev/null 2>&1
  rm -rf /tmp/snap && mkdir -p /tmp/snap
  mc mirror --quiet local/$BUCKET /tmp/snap >/dev/null 2>&1
  echo \"  \$(mc ls -r local/$BUCKET | wc -l) 개\"
"
docker compose cp "minio:/tmp/snap/." "$OUT/images/" >/dev/null

echo
echo "완료 → $OUT"
echo "  이 폴더를 통째로 새 PC 의 BE/ 아래에 두고 import.sh 를 실행하세요."
