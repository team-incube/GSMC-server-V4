# 헥사고날 아키텍처 가이드

> 이 프로젝트에 처음 기여하는 분을 위한 구조 안내서입니다.

---

## 한 줄 요약

**"비즈니스 로직(도메인)을 DB·HTTP 같은 기술 구현과 완전히 분리한다."**

---

## 왜 이 구조를 쓰나요?

```
일반적인 레이어드 구조           헥사고날 구조
────────────────────           ─────────────────────
Controller                     adapter/web  (REST)
    ↓                               ↓
Service          ←→ 비즈니스    port/in → service → port/out
    ↓                               ↓
Repository                     adapter/out (JPA, Redis)
```

레이어드 구조에서는 Service가 JPA를 직접 알고 있어서,
DB를 바꾸거나 테스트할 때 전체를 고쳐야 합니다.

헥사고날 구조에서는 Service가 **인터페이스(Port)만 알고**,
실제 구현(Adapter)은 바깥에서 교체할 수 있습니다.

---

## 폴더 구조

```
domain/auth/
├── AuthorizationUrlResult.kt       ← 도메인 모델 (순수 Kotlin)
├── TokenResult.kt
│
├── port/
│   ├── in/                         ← 인바운드 포트 (UseCase 인터페이스)
│   │   ├── SigninUseCase.kt
│   │   └── SignoutUseCase.kt
│   │
│   └── out/                        ← 아웃바운드 포트 (저장소/외부 인터페이스)
│       ├── UserPersistencePort.kt
│       ├── RefreshTokenPersistencePort.kt
│       └── OAuthPort.kt
│
├── service/                        ← 비즈니스 로직 (UseCase 구현체)
│   ├── LoginService.kt
│   └── SignoutService.kt
│
└── adapter/
    ├── web/                        ← REST 컨트롤러
    │   └── AuthWebAdapter.kt
    │
    └── out/
        ├── persistence/            ← JPA 어댑터
        │   ├── AuthUserPersistenceAdapter.kt
        │   ├── RefreshTokenPersistenceAdapter.kt
        │   └── repository/
        │       └── UserJpaRepository.kt
        └── oauth/                  ← 외부 API 어댑터
            └── DataGsmOAuthAdapter.kt
```

---

## 각 계층의 역할

### 도메인 모델 (`domain/`)

순수 Kotlin 데이터 클래스. JPA도 Spring도 모릅니다.

```kotlin
// ✅ 이렇게 — 아무것도 의존하지 않음
data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
    val role: UserRole,
)

// ❌ 이렇게는 안 됨
@Entity  // JPA 어노테이션은 도메인에 쓰지 않음
data class TokenResult(...)
```

---

### 인바운드 포트 (`port/in/`) — UseCase 인터페이스

"이 도메인이 외부에 제공하는 기능"을 인터페이스로 선언합니다.
`adapter/web`이 호출하고, `service`가 구현합니다.

```kotlin
// SigninUseCase.kt
interface SigninUseCase {
    fun execute(code: String, state: String, redirectUri: String): TokenResult
}
```

---

### 아웃바운드 포트 (`port/out/`) — 저장소/외부 인터페이스

"이 도메인이 외부에 요청하는 것"을 인터페이스로 선언합니다.
`service`가 호출하고, `adapter/out`이 구현합니다.

```kotlin
// RefreshTokenPersistencePort.kt
interface RefreshTokenPersistencePort {
    fun save(userId: Long, refreshToken: String)
    fun find(userId: Long): String?
    fun delete(userId: Long)
}
```

> 이 인터페이스 덕분에 Service는 Redis인지 RDB인지 모릅니다.

---

### 서비스 (`service/`) — 비즈니스 로직

UseCase(port/in)를 구현하고, Port(port/out)를 통해서만 외부와 통신합니다.
`@Transactional`은 여기에만 붙입니다.

```kotlin
// LoginService.kt
class LoginService(
    private val oAuthPort: OAuthPort,                           // port/out 주입
    private val userPersistencePort: UserPersistencePort,       // port/out 주입
    private val refreshTokenPersistencePort: RefreshTokenPersistencePort,
    private val authTokenPort: AuthTokenPort,
) : SigninUseCase {                                             // port/in 구현

    @Transactional
    override fun execute(code: String, state: String, redirectUri: String): TokenResult {
        // 비즈니스 로직만 작성
        val oAuthToken = oAuthPort.exchangeCodeForToken(code, redirectUri, codeVerifier)
        val user = userPersistencePort.findByEmail(email) ?: userPersistencePort.save(newUser)
        // ...
    }
}
```

---

### 인바운드 어댑터 (`adapter/web/`) — REST 컨트롤러

외부(REST)로부터 요청을 받아 UseCase를 호출합니다.
비즈니스 로직은 전혀 없습니다.

```kotlin
// AuthWebAdapter.kt
@RestController
class AuthWebAdapter(
    private val signinUseCase: SigninUseCase,  // Service가 아닌 UseCase 인터페이스를 주입
) {
    @PostMapping("/api/auth/signin")
    fun signin(@RequestBody input: LoginInput): ResponseEntity<TokenResult> =
        ResponseEntity.ok(signinUseCase.execute(input.code, input.state, input.redirectUri))
}
```

---

### 아웃바운드 어댑터 (`adapter/out/`) — JPA / Redis / 외부 API

Port 인터페이스를 구현하고, 실제 기술(JPA, Redis, HTTP)을 사용합니다.

```kotlin
// RefreshTokenPersistenceAdapter.kt — Redis 구현체
class RefreshTokenPersistenceAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : RefreshTokenPersistencePort {  // port/out 구현

    override fun save(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set("refresh:$userId", refreshToken)
    }
}
```

도메인 ↔ JPA 엔티티 변환은 **확장 함수**로 처리합니다.

```kotlin
// UserJpaEntityExtensions.kt
fun UserJpaEntity.toDomain(): User = User(userId = id, userName = userName, ...)
fun User.toEntity(): UserJpaEntity = UserJpaEntity(userName = userName, ...)
```

---

## 의존성 방향 (핵심)

```
adapter/web  →  port/in  →  service  →  port/out  ←  adapter/out
               (UseCase)              (Persistence)
```

- 화살표는 "알고 있다(의존한다)"는 뜻입니다.
- `service`는 `adapter/out`을 모릅니다. `port/out` 인터페이스만 압니다.
- `adapter/out`은 `service`를 모릅니다. `port/out`을 구현할 뿐입니다.

---

## 새 기능을 추가할 때 순서

예) "회원 탈퇴" 기능을 추가한다면:

1. **도메인 모델** — 필요하면 `domain/` 에 데이터 클래스 추가
2. **port/in** — `DeleteMemberUseCase` 인터페이스 작성
3. **port/out** — 필요한 저장소 인터페이스 작성 (이미 있으면 생략)
4. **service** — `RemoveMemberService` 비즈니스 로직 작성
5. **adapter/out** — JPA 어댑터에 삭제 로직 추가
6. **adapter/web** — REST 컨트롤러에 엔드포인트 추가
7. **GraphQL 스키마** — GraphQL 도메인이라면 `*.graphqls` 에 타입/뮤테이션 정의

---

## 네이밍 치트시트

| 계층 | 예시 |
|------|------|
| UseCase (port/in) | `FetchMemberUseCase`, `AppendScoreUseCase` |
| PersistencePort (port/out) | `MemberPersistencePort` |
| Service | `FetchMemberService`, `RemoveScoreService` |
| WebAdapter (adapter/web) | `MemberWebAdapter` |
| PersistenceAdapter (adapter/out) | `MemberPersistenceAdapter` |
| JpaEntity | `MemberJpaEntity` |
| JpaRepository | `MemberJpaRepository` |
| RedisRepository | `BlackListRedisRepository` |

> 행위별 서비스 접두어: 조회 `Fetch` / 검색 `Search` / 수정 `Modify` / 생성 `Append` / 삭제 `Remove`

---

## 자주 하는 실수

| 실수 | 올바른 방법 |
|------|------------|
| `service`에서 `JpaRepository`를 직접 주입 | `port/out` 인터페이스를 주입 |
| `adapter/web`에 비즈니스 로직 작성 | 로직은 `service`로 이동 |
| `domain` 모델에 `@Entity` 사용 | `adapter/out/entity`에 별도 JpaEntity 작성 |
| `repository`에 `@Transactional` 사용 | `service`에만 `@Transactional` 사용 |
| `Service` 클래스를 직접 주입 (`LoginService`) | UseCase 인터페이스를 주입 (`SigninUseCase`) |
