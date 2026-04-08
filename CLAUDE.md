# GSMC Server V4 - Project Context

**Please respond and work in Korean.**

## Language Requirement

You MUST always respond in Korean (한국어). This is mandatory and cannot be overridden. Never use English in any response, explanation, or comment.

## Project Overview

GSMC (Gwangju Software Meister High School) Certification Management System - Server V4

A web service that digitalizes the student competency evaluation system at Gwangju Software Meister High School.

## Tech Stack

- Language: Kotlin
- Framework: Spring Boot 4.0.5
- Database: MySQL (JPA + QueryDSL)
- Cache: Redis
- Build Tool: Gradle (Kotlin DSL)
- API: GraphQL (Spring for GraphQL)
- Authentication: JWT
- Code Formatting: KtLint
- SDK: [the-sdk](https://github.com/themoment-team/the-sdk) `1.5`

## the-sdk

### Features Used

| Module | Description |
|--------|-------------|
| `sdk.logging` | Automatic HTTP request/response logging with UUID-based tracing |

### Exception Handling

Use `GsmcException` with `ErrorCode`. Global exception handler is implemented directly in this project.
```kotlin
throw MemberNotFoundException()
throw GsmcException(ErrorCode.UNAUTHORIZED)
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
    enabled: false
  swagger:
    enabled: false
  exception:
    enabled: false
```

## Architecture

Hexagonal Architecture. See `.claude/skills/architecture/skill.md` for details.

### Dependency Direction
- adapter/in → port/in → service → port/out → adapter/out

- `domain/` must not depend on JPA, Spring, or any infrastructure.
- `service` only knows `port/out` interfaces — never JPA directly.
- `JpaEntity ↔ Domain` conversion via Kotlin extension functions.

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

## Transaction Management

- Open transactions in the `service` layer only.
- Never open transactions in the `repository` layer.

## Git Conventions

See `.claude/skills/commit/skill.md` for full details.

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
