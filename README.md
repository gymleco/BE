# api — 백엔드 / CMS (Spring Boot 3 / Java 21)

Phase 2 에서 여기에 Spring Boot 프로젝트를 스캐폴딩한다. 현재는 비어 있다.

로컬 확인된 JDK: **17.0.12 (Temurin/Oracle LTS)** — 기획서 §5 와 일치.

## 담당 범위

| | |
|---|---|
| 공개 API | 제품 조회, 소식 조회, 문의 접수 |
| 관리 API | 제품/소식 CRUD, 문의 관리, 설정 — 전부 `@PreAuthorize("hasRole('ADMIN')")` |
| 인증 | Spring Security + JWT (HttpOnly / Secure / SameSite=Strict 쿠키) |
| 저장소 | PostgreSQL, S3 |
| 부가 | 이미지 재인코딩, HTML 살균, 개인정보 암호화, 감사 로그, 자동 파기 배치 |

## 설계상 반드시 지킬 것

- **CSRF 를 켠다.** 쿠키 인증으로 가는 순간 CSRF 대상이 된다.
  `SameSite=Strict` 하나에만 의존하지 말고 `CookieCsrfTokenRepository` 를 함께 쓴다.
- **관리 API 마다 `@PreAuthorize`.** 하나만 빠지면 그게 구멍이다.
  배포 전 엔드포인트를 세면서 확인한다.
- **CMS 저장 후 ISR 재검증 호출.** 커밋 완료 뒤
  `REVALIDATE_URL` 로 토큰과 함께 POST 한다.
- **업로드 이미지는 재인코딩 후 S3 로.** 앱 서버가 파일을 서빙하지 않는다.
- **리치 텍스트는 저장 시점에 살균.** OWASP Java HTML Sanitizer, 허용목록 방식.
- **전화번호는 암호화 + 블라인드 인덱스.** CSV 내보내기는 감사 로그를 남긴다.

## 시작

```bash
cp .env.example .env
```
# BE
