---
name: security-checklist
description: 하드코딩된 시크릿·SQL 인젝션·JWT 검증·OAuth 흐름·민감 정보 로깅·인가(Authorization) 여부를 점검. auth·security·member 관련 PR을 머지하기 전에 필수로 실행.
allowed-tools: Bash, Grep, Read
---

# Security Checklist

`auth`, `security`, `member` 도메인이나 인증/인가 관련 코드를 수정했을 때 머지 전에 아래 항목을 순서대로 점검한다.

## 1. 하드코딩된 시크릿

- [ ] 코드에 API Key, Secret, Password, 클라이언트 시크릿이 직접 박혀 있지 않은가?
- [ ] `application.yaml`의 값이 전부 `${ENV_VAR}` 형태로 환경 변수를 참조하는가? (예: `${OAUTH_CLIENT_SECRET}`, `${JWT_SECRET}`)

검증 명령:
```bash
# Kotlin 파일 내 하드코딩 의심 문자열
grep -rnE '(password|secret|apiKey|clientSecret)\s*=\s*"[^$]' --include="*.kt" src/main

# application.yaml에 실제 값이 박혀있는지 (환경변수 참조가 아닌 리터럴)
grep -nE 'secret|password|client-secret' src/main/resources/application.yaml
```

**한계**: base64/난독화된 값이나 외부 설정 서버에서 주입되는 값은 grep으로 못 잡는다. `auth`, `security` 패키지는 직접 눈으로도 확인할 것.

## 2. SQL 인젝션

- [ ] JPA/QueryDSL만 사용하고 native query에 문자열을 직접 이어붙이지 않았는가?
- [ ] `@Query`에 파라미터를 바인딩(`:param`)했지, 문자열 포맷팅으로 넣지 않았는가?

```bash
grep -rn "createNativeQuery\|@Query.*+" --include="*.kt" src/main
```

## 3. JWT / OAuth 검증

이 프로젝트는 `JwtTokenProvider`(`global/security/jwt`)와 DataGsm OAuth(`domain/auth/adapter/out/oauth`)를 사용한다.

- [ ] 토큰 파싱 시 `Jwts.parser().verifyWith(signingKey)`로 서명을 검증하는가? (서명 검증 없이 `parseClaims`만 호출하지 않았는지)
- [ ] 만료(`expiration`)를 검증하는가? — 서명 검증 라이브러리(jjwt)가 자동으로 `ExpiredJwtException`을 던지므로, 이를 삼키지 않고 `GsmcException(ErrorCode.EXPIRED_TOKEN)` 등으로 변환하는가?
- [ ] `role` 같은 커스텀 claim을 꺼낼 때 null-safe하게 처리하고, 실패 시 `GsmcException(ErrorCode.INVALID_TOKEN)`을 던지는가?
- [ ] OAuth `state` 파라미터를 `OAuthStatePersistencePort`로 검증해 CSRF를 막는가?
- [ ] `redirect_uri`가 화이트리스트(`ErrorCode.INVALID_REDIRECT_URI`)로 제한되는가?

```bash
find src/main -path "*/security/jwt/*" -name "*.kt"
find src/main -path "*/domain/auth/*" -name "*.kt"
```

## 4. 리프레시 토큰 / 세션 관리

- [ ] 리프레시 토큰이 DB/Redis에 저장되고, 로그아웃 시 `RemoveMyRefreshTokenService` 등으로 명시적으로 제거되는가?
- [ ] 리프레시 토큰 재사용(탈취 후 재사용) 탐지 로직이 있는가, 최소한 만료 시간이 짧게 설정되어 있는가?

## 5. 민감 정보 로깅

- [ ] `password`, `token`, `secret`, `refreshToken` 값이 `log.info`/`log.debug`에 그대로 찍히지 않는가?
- [ ] the-sdk의 `sdk.logging`(HTTP 요청/응답 자동 로깅)이 Authorization 헤더나 요청 바디의 토큰 값을 그대로 남기지 않는지 확인 — 마스킹이 필요하면 `not-logging-urls` 또는 별도 필터로 제외

```bash
grep -rn "log\.\(info\|debug\|warn\)" --include="*.kt" src/main | grep -iE "token|password|secret"
```

## 6. 인가 (Authorization)

- [ ] 로그인 필요 엔드포인트가 Spring Security 필터 체인(`JwtAuthenticationFilter`)을 거치는가?
- [ ] 본인 리소스만 접근하는 API(`My` 키워드가 붙은 서비스)는 `SecurityContextHolder`/`MemberUtil.getCurrentUserId()`로 요청자 ID를 가져와 소유권을 검증하는가? (파라미터로 넘어온 ID를 그대로 신뢰하지 않는가)
- [ ] `developer` 도메인처럼 타인의 데이터를 다루는 API는 서비스 메서드 진입 시점에 `UserRole.ROOT`를 검증하는가? (자세한 규칙은 [[architecture]] 스킬의 "Developer-only APIs" 참고)
- [ ] DB 유니크 제약에만 기대지 않고, 조회 후 존재 여부를 먼저 검증해 적절한 `GsmcException`을 던지는가?

## 참고 파일 탐색

```bash
find src/main -name "GsmcException.kt" -o -name "ErrorCode.kt" -o -name "GsmcExceptionResolver.kt"
find src/main -path "*/security/*" -name "*.kt"
```

## 리포트 형식

각 항목을 ✓(통과) / ⚠(권고) / ✗(수정 필요)로 표시하고, 마지막에 총 n개 중 통과/경고/오류 개수를 요약한다.
