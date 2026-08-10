# CLAUDE.md — gymleco BE

Spring Boot 4.1.0 / Java 21 API 서버. 프론트는 별도 저장소(`gymleco-fe`).

---

## ⚠️ 작업 분담 (가장 중요)

| 대상 | 방식 |
|---|---|
| **`.java` 파일** | **절대 디스크에 쓰지 않는다.** 채팅으로 코드를 출력하고 개발자가 직접 검수·타이핑한다. |
| **그 외 전부** | Claude 가 직접 작성한다 — `build.gradle`, `application.yml`, `Dockerfile`, `compose.yaml`, **마이그레이션 SQL**, nginx·k8s 설정, 디렉터리 구조 |

**`.java` 에 Write / Edit 툴을 쓰지 않는다.** 백엔드 로직은 개발자가 직접
타이핑하며 익히기 위한 것이므로, 파일을 만들어 버리면 목적이 사라진다.

`.java` 코드를 출력할 때는:
- 파일 경로를 먼저 명시한다 (`src/main/java/kr/co/gymleco/...`)
- 한 번에 한 덩어리씩, 타이핑 가능한 분량으로 끊는다
- 왜 그렇게 쓰는지 짧게 덧붙인다 (특히 보안 관련 부분)

---

## 스택 주의

**Spring Boot 4.1.0 / Spring Security 7 / Jackson 3** 이다. Boot 3 과 다르다.

- Security 7 은 **람다 DSL 만** 지원한다. 비-람다 오버로드는 제거됐다.
- Jackson 이 3.0 으로 올라갔다 (Jackson 2 는 deprecated). 패키지명이 다르므로
  애너테이션을 쓸 일이 있으면 반드시 확인하고 쓴다.
- **기억으로 Boot 3 문법을 쓰지 않는다.** 확인 후 출력한다 —
  틀린 코드는 타이핑하는 사람의 시간을 그대로 버린다.

로컬에 JDK 21 이 없어도 된다. `docker build` 가 JDK 21 + Gradle 을 갖고 있다.

---

## 보안 규칙 (예외 없음)

1. **시크릿을 커밋하지 않는다.** 올라간 키는 삭제로 끝나지 않는다 — 재발급한다.
2. **`--no-verify` 로 pre-commit 훅을 우회하지 않는다.**
3. **관리 API 를 추가하면 `@PreAuthorize("hasRole('ADMIN')")` 를 같이 추가한다.**
   하나만 빠져도 그게 구멍이다. 배포 전 엔드포인트를 세면서 확인한다.
4. **비밀번호는 BCrypt 만.** MD5·SHA·평문 금지.
   DB 의 CHECK 제약이 BCrypt 가 아닌 해시를 거부하도록 돼 있다.
5. **리치 텍스트는 저장 시점에 살균한다** (OWASP sanitizer, 허용목록 방식).
6. **업로드 이미지는 시그니처 검증 → 재인코딩 → 오브젝트 스토리지.**
   앱 서버가 파일을 서빙하지 않는다.
7. **전화번호는 AES-256-GCM + 블라인드 인덱스.** 결정적 암호화 금지.
8. **CSV 로 내보낼 때 수식 인젝션을 막는다** (`=` `+` `-` `@` 이스케이프)
   그리고 내보낸 사실을 `admin_audit_log` 에 남긴다.
   컬럼 암호화를 무력화하는 유일한 통로다.
9. **로그에 비밀번호·토큰·개인정보를 남기지 않는다.**

---

## 트랜잭션 방침

| 항목 | 규칙 |
|---|---|
| `open-in-view` | **false** (설정 완료). 되돌리지 않는다 |
| 조회 메서드 | `@Transactional(readOnly = true)` |
| 외부 호출 | `@TransactionalEventListener(phase = AFTER_COMMIT)` — ISR 재검증·메일 발송을 트랜잭션 안에서 하지 않는다. 롤백된 데이터로 사이트가 갱신된다 |
| 컬렉션 + 페이징 | 조인 페치와 페이징을 함께 쓰지 않는다 (설정이 예외로 잡아준다) |

---

## 스키마

스키마의 소유자는 **Flyway** 다. Hibernate 는 `ddl-auto: validate` 로만 동작한다.
스키마를 바꾸려면 새 `V{n}__*.sql` 을 추가한다. **기존 마이그레이션을 수정하지 않는다.**

CI 가 모든 마이그레이션을 실제 PostgreSQL 에 적용해 검증한다.

---

## 문서

설계 문서는 org 의 [`.github`](https://github.com/GYMLECO-KOREA/.github) 저장소에 있다.
`docs/03-database.md` (ERD·개인정보 처리), `docs/SECURITY-CHECKLIST.md` 를 참고한다.
