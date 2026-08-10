# ══════════════════════════════════════════════════════════════
#  gymleco-api — 멀티스테이지 빌드
#
#  로컬에 JDK 21 이나 Gradle 을 설치하지 않아도 된다.
#  빌드 이미지가 둘 다 갖고 있다.
#
#  빌드:  docker build -t gymleco-api:local BE/
# ══════════════════════════════════════════════════════════════

# ── 1단계: 빌드 ────────────────────────────────────────────────
FROM gradle:9-jdk21-ubi9 AS build
WORKDIR /workspace

# 의존성만 먼저 해석해 레이어 캐시를 살린다.
# 소스만 바뀌었을 때 의존성을 다시 받지 않는다.
COPY --chown=gradle:gradle settings.gradle build.gradle ./
RUN gradle --no-daemon dependencies --refresh-dependencies || true

COPY --chown=gradle:gradle src ./src
RUN gradle --no-daemon clean bootJar -x test

# ── 2단계: 실행 ────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

# root 로 돌리지 않는다. 컨테이너가 뚫려도 권한이 남지 않게 한다.
RUN addgroup -S app && adduser -S -G app app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/build/libs/app.jar ./app.jar

USER app

EXPOSE 8080 8081

# 컨테이너 메모리에 맞춰 힙을 잡는다. 고정 -Xmx 보다 안전하다.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Djava.security.egd=file:/dev/urandom"

# 액추에이터는 8081(내부 전용)에 있다.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:8081/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
