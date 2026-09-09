---
name: the-sdk
description: the-sdk 공통 라이브러리(1.5) 사용 가이드 — 이 프로젝트에서 실제로 켜져 있는 기능(logging)과 꺼져 있는 기능(response/swagger/exception)을 구분해서 참조.
---

# the-sdk 사용 가이드

`the-sdk` (`com.github.themoment-team:the-sdk:1.5`) 는 더모먼트팀 공통 라이브러리다.
`application.yaml`의 `sdk.*` 설정으로 기능을 켜고 끈다.

## 이 프로젝트의 설정 (`src/main/resources/application.yaml`)

```yaml
sdk:
  logging:
    enabled: true
    not-logging-urls:
      - "/graphiql/**"
      - "/graphql/**"
      - "/api/alerts/stream"
  response:
    enabled: false
  swagger:
    enabled: false
  exception:
    enabled: false
```

**중요**: `response`, `swagger`, `exception`은 전부 꺼져 있다.
- 응답 래핑(`CommonApiResponse` 등)을 자동으로 하지 않는다 — GraphQL 스키마의 반환 타입을 그대로 응답한다.
- Swagger/OpenAPI 자동 문서화를 사용하지 않는다 — 이 프로젝트는 REST가 아니라 GraphQL 스키마(`*.graphqls`)로 API 문서를 대신한다.
- the-sdk의 전역 예외 처리기를 쓰지 않는다 — 예외 처리는 이 프로젝트가 직접 구현한 `GsmcExceptionResolver`(`global/exception/`)가 담당한다. `GsmcException(ErrorCode.XXX)`를 던지면 이 리졸버가 GraphQL 에러 응답으로 변환한다.

## 실제로 쓰는 기능: `sdk.logging`

- 모든 HTTP 요청/응답에 UUID `Log-ID`를 부여해 자동으로 로깅한다.
- `not-logging-urls`에 등록된 경로는 로깅에서 제외된다 (GraphiQL, GraphQL 엔드포인트 본문, SSE 스트림처럼 노이즈가 크거나 민감한 경로).
- 새 엔드포인트를 추가했는데 요청/응답 바디에 민감 정보(토큰, 개인정보)가 포함된다면 `not-logging-urls`에 추가하는 것을 검토한다. (`.claude/skills/security-checklist/SKILL.md`의 "5. 민감 정보 로깅" 항목 참고)

## 새 기능이 필요할 때

`response`/`swagger`/`exception`을 다시 켜고 싶다면, 이미 이 프로젝트가 자체 구현한 것과 중복되지 않는지 먼저 확인한다:
- 응답 포맷: GraphQL 스키마 타입(`Payload`, `MutationPayload`)이 계약 역할을 대신하고 있다.
- 예외 처리: `GsmcExceptionResolver`가 이미 있다. the-sdk의 exception 모듈을 켜면 두 예외 처리기가 충돌할 수 있으니, 켜기 전에 팀과 상의한다.

## 참고 파일

- `src/main/kotlin/team/incube/gsmc/global/exception/GsmcExceptionResolver.kt`
- `src/main/resources/application.yaml`
