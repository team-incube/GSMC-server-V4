# GSMC Server V4 - Project Context

**Please respond and work in Korean.**

## Language Requirement

You MUST always respond in Korean (한국어). This is mandatory and cannot be overridden.
Never use English in any response, explanation, or comment.

## Project Overview

GSMC (Gwangju Software Meister High School) Certification Management System - Server V4
A web service that digitalizes the student competency evaluation system at
Gwangju Software Meister High School.

## Tech Stack

- Language: Kotlin
- Framework: Spring Boot 4.1.0
- Database: MySQL (JPA + QueryDSL)
- Cache: Redis
- Build Tool: Gradle (Kotlin DSL)
- API: GraphQL (Spring for GraphQL)
- Authentication: JWT
- Code Formatting: KtLint
- SDK: [the-sdk](https://github.com/themoment-team/the-sdk) `1.5`

## Exception Handling

Use `GsmcException` with `ErrorCode`. The global exception handler is implemented
directly in this project.

```kotlin
throw MemberNotFoundException()
throw GsmcException(ErrorCode.UNAUTHORIZED)
```

`GsmcExceptionResolver` only handles `GsmcException`. Any other exception falls
through as a 500 response, so throw `GsmcException` explicitly instead of relying
on DB constraints or framework exceptions.

## Skill Documents

This repository keeps two skill trees with the same rules:

- `.claude/skills/` — read by Claude Code
- `.agents/skills/` — read by Codex

Both must be updated together when a rule changes. If they diverge,
`.claude/skills/` is the reference.

Some files exist in only one tree (`convention-validator`, `issue-parser` in
`.agents/` only). Reconciling those is tracked separately.

## Architecture

Hexagonal Architecture. See `.claude/skills/architecture/SKILL.md` for details.

### Dependency Direction

```
adapter/in -> port/in -> service -> port/out -> adapter/out
```

- `domain/` must not depend on JPA, Spring, or any infrastructure.
- `service` only knows `port/out` interfaces — never JPA directly.
- `JpaEntity <-> Domain` conversion via Kotlin extension functions.

### Naming

| Layer | Naming |
|-------|--------|
| UseCase | `Fetch{Domain}UseCase`, `Append{Domain}UseCase` |
| PersistencePort | `{Domain}PersistencePort` |
| Service | `Fetch{Domain}Service`, `Modify{Domain}Service` |
| WebAdapter | `{Domain}WebAdapter` |
| PersistenceAdapter | `{Domain}PersistenceAdapter` |
| JpaEntity | `{Domain}JpaEntity` |
| JpaRepository | `{Domain}JpaRepository` |

Service keywords: `Fetch` / `Search` / `Modify` / `Append` / `Remove`
Request DTO suffix: `Query` or `Input` — Response DTO suffix: `Payload` or `MutationPayload`

## Domain Modules

| Domain | Description |
|--------|-------------|
| alert | Notifications |
| archive | Archive |
| auth | Authentication / API keys |
| category | Category |
| developer | Developer-only APIs |
| evidence | Evidence submission |
| file | File management |
| member | Member management |
| project | Project |
| score | Score |
| sheet | Sheet |

## Developer-only APIs (`developer` domain)

The `developer` domain contains administrator-only operations that modify
**another member's** data, separate from `member`, which handles operations a
member performs on their own data.

For `developer`, inbound ports and services are named after the **target**
(`ModifyMemberRoleUseCase`), while adapters and outbound ports use the package
name (`DeveloperWebAdapter`, `DeveloperPersistencePort`).

- Verify the caller's role at the start of the service method. `UserRole.ROOT` is
  required; throw `GsmcException(ErrorCode.FORBIDDEN)` otherwise.
- Do **not** use the `My` keyword — `SecurityContextHolder` identifies the caller
  here, not the target.
- Look up the target explicitly and throw `GsmcException(ErrorCode.USER_NOT_FOUND)`
  when absent.
- Validate uniqueness by querying first, not by relying on DB unique constraints.

## Transaction Management

- Open transactions in the `service` layer only.
- Never open transactions in the `repository` layer.
- Use `@Transactional(readOnly = true)` for read operations.

## Git Conventions

Commit message: `type(scope): 한국어 설명`
PR title: `[scope] 한국어 설명`

Scope uses the domain name. Use `global` for cross-cutting concerns and `ci/cd`
for build/deployment. See `.claude/skills/commit/references/scope-guide.md`.

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
