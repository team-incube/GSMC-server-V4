# GSMC Server V4 - Project Context

## Language Requirement
**ALL responses from Claude MUST be in Korean (한국어).**

## Project Overview

GSMC (Gwangju Software Meister High School) Certification Management System - Server V4

A web service that digitalizes the student competency evaluation system at Gwangju Software Meister High School.

## Tech Stack

- Language: Kotlin
- Framework: Spring Boot 3.x
- Database: MySQL (JPA)
- Cache: Redis
- Build Tool: Gradle (Kotlin DSL)
- API: GraphQL (Spring for GraphQL)
- Authentication: JWT
- Code Formatting: KtLint
- SDK: [the-sdk](https://github.com/themoment-team/the-sdk) `1.5`

## the-sdk

This project uses `the-sdk` — a modular SDK for reusable Spring Boot components by themoment-team.

### Installation
```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.themoment-team:the-sdk:1.5")
}
```

### Features Used

| Module | Description |
|--------|-------------|
| `sdk.logging` | Automatic HTTP request/response logging with UUID-based tracing |
| `sdk.response` | Consistent API response wrapping via `CommonApiResponse` |

### Exception Handling

Use `GsmcException` with `ErrorCode` for all exceptions. Global exception handler is implemented directly in this project to customize GraphQL error response format.
```kotlin
// Define error codes
enum class ErrorCode(val status: HttpStatus, val message: String) {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
}

// Domain-specific exceptions
class MemberNotFoundException : GsmcException(ErrorCode.MEMBER_NOT_FOUND)

// Throw exceptions
throw MemberNotFoundException()
throw GsmcException(ErrorCode.UNAUTHORIZED)
```

```yaml
sdk:
  exception:
    enabled: false
```
### application.yml Configuration
```yaml
sdk:
  logging:
    enabled: true
    not-logging-urls:
      - "/graphiql/**"
      - "/graphql/**"
  response:
    enabled: true
    not-wrapping-urls:
      - "/graphql/**"
      - "/graphiql/**"
  exception:
    enabled: false
```

## Architecture

This project follows Hexagonal Architecture. See `.claude/skills/architecture/skill.md` for details.

### Package Structure
```
src/main/kotlin/team/incube/gsmc/
├── domain/
│   └── {domain}/
│       ├── application/
│       │   ├── port/
│       │   │   ├── in/          # UseCase interfaces
│       │   │   └── out/         # PersistencePort interfaces
│       │   └── service/         # Business logic
│       ├── domain/              # Pure domain model (no JPA, no Spring)
│       ├── exception/           # Domain exceptions
│       └── adapter/
│           ├── in/              # WebAdapter (GraphQL)
│           └── out/
│               └── persistence/
│                   ├── entity/
│                   ├── repository/
│                   └── {Domain}PersistenceAdapter.kt
├── global/
│   ├── config/
│   ├── exception/
│   └── security/
└── GsmcApplication.kt
```

### Dependency Direction
```
adapter/in → port/in → service → port/out → adapter/out
```

- `domain/` must not depend on JPA, Spring, or any infrastructure.
- `service` only knows `port/out` interfaces — never JPA directly.
- `JpaEntity ↔ Domain` conversion is handled via Kotlin extension functions in `adapter/out/persistence`.

## Domain Modules

| Domain | Description |
|--------|-------------|
| alert | Notifications |
| archive | Archive |
| auth | Authentication / API keys |
| category | Category |
| developer | Developer info |
| evidence | Evidence submission |
| file | File management |
| member | Member management |
| project | Project |
| score | Score |
| sheet | Sheet |

## Naming Conventions

See `.claude/skills/convention/skill.md` for full details.

### Service

| Action | Keyword | Example |
|--------|---------|---------|
| Fetch | Fetch | `FetchMemberService` |
| Search | Search | `SearchProjectService` |
| Update | Modify | `ModifyMemberService` |
| Create | Append | `AppendScoreService` |
| Delete | Remove | `RemoveScoreService` |

- Add `My` keyword when using `SecurityContextHolder`
    - e.g. `FetchMyScoreService`, `ModifyMyCertificateService`

### DTO

| Action | Request | Response |
|--------|---------|----------|
| Fetch | `GetUserQuery` | `UserPayload` |
| Search | `SearchUserQuery` | `UserPayload` |
| Create | `CreateUserInput` | `UserMutationPayload` |
| Update | `UpdateUserInput` | `UserMutationPayload` |
| Delete | `DeleteUserInput` | `UserMutationPayload` |

### GraphQL Controller
```kotlin
// With return value
@QueryMapping
fun member(@Argument id: Long): MemberPayload {
    return fetchMemberUseCase.execute(id)
}

// Without return value - explicitly return CommonApiResponse
@MutationMapping
fun deleteMember(@Argument id: Long): CommonApiResponse<Unit> {
    removeMemberUseCase.execute(id)
    return CommonApiResponse.success("deleted successfully")
}
```

Controller parameter must be named `input`.

## Transaction Management

- Open transactions in the `service` layer only.
- Never open transactions in the `repository` layer.

## Git Conventions

See `.claude/skills/commit/skill.md` for full details.

### Commit Types

| Type | Usage |
|------|-------|
| feat | Add new code or files |
| refactor | Modify existing code |
| fix | Bug fix |
| delete | Delete code or files |
| docs | Documentation |
| test | Add/modify tests |
| merge | Merge branch |
| init | Project initialization |

### Branch Strategy

- `main`: Production
- `develop`: Integration branch
- `feat/{name}`: Feature
- `fix/{issue}`: Bug fix
- `refactor/{description}`: Refactoring

## New Feature Checklist

- [ ] Define GraphQL schema (`*.graphqls`)
- [ ] Create domain model (`domain/`)
- [ ] Create UseCase interface (`port/in/`)
- [ ] Create PersistencePort interface (`port/out/`)
- [ ] Create Service implementation (`service/`)
- [ ] Create JpaEntity (`adapter/out/persistence/entity/`)
- [ ] Create JpaRepository (`adapter/out/persistence/repository/`)
- [ ] Create PersistenceAdapter (`adapter/out/persistence/`)
- [ ] Create WebAdapter (`adapter/in/`)
- [ ] Apply KtLint formatting


**Remember**: All responses MUST be in Korean (한국어)! 🇰🇷
