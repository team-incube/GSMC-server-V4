---
name: docker
description: 이 프로젝트의 Dockerfile / docker-compose.yml 작성·수정 가이드 — 멀티스테이지 빌드, 레이어 캐싱, 헬스체크, 시크릿을 코드에 넣지 않는 규칙.
---

# Docker 가이드

## 현재 Dockerfile

```dockerfile
FROM gradle:9.4-jdk25 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

빌드 스테이지는 JDK 전체 이미지(`gradle:9.4-jdk25`)를 쓰고, 실행 스테이지는 JRE만 있는 가벼운 이미지(`eclipse-temurin:25-jre-jammy`)로 분리해 최종 이미지 크기를 줄인다.

### 개선 여지 (변경 시 고려)

- 현재 `COPY . .`을 의존성 설치 전에 실행해 소스 변경마다 Gradle 캐시가 무효화된다. 캐싱을 최적화하려면:
  ```dockerfile
  COPY gradlew settings.gradle.kts build.gradle.kts ./
  COPY gradle gradle
  RUN ./gradlew dependencies --no-daemon
  COPY src src
  RUN ./gradlew bootJar --no-daemon
  ```
- 현재 컨테이너가 root로 실행된다. 운영 환경 보안 강화가 필요하면 non-root 사용자 추가를 검토:
  ```dockerfile
  RUN groupadd -r app && useradd -r -g app app
  USER app
  ```
이 두 가지는 기존 동작을 바꾸는 변경이므로, 팀 논의 없이 임의로 적용하지 않는다.

## docker-compose.yml

```yaml
services:
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
    depends_on:
      mysql:
        condition: service_healthy

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${RDB_PASSWORD}
      MYSQL_DATABASE: ${RDB_SCHEMA}
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${RDB_PASSWORD}"]
```

- 모든 자격증명은 `.env` 파일을 통한 환경 변수로만 주입한다 (`${VAR}` 형태). `docker-compose.yml`에 실제 비밀번호를 리터럴로 적지 않는다 — `.claude/skills/security-checklist/SKILL.md`의 "1. 하드코딩된 시크릿" 항목 참고.
- 새 서비스(Redis 등)를 추가할 때도 같은 패턴: `depends_on` + `healthcheck` + 환경 변수 주입.
- `.env`, `.env.local` 등은 반드시 `.gitignore`에 포함되어 있어야 한다. 커밋 전 `git status`로 실수 포함 여부 확인.

## 참고 파일

- `Dockerfile`
- `docker-compose.yml`
