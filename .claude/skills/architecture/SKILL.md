---
name: architecture
description: Hexagonal architecture structure and rules for this project. Use this when creating new files or features to place them in the correct layer.
---

## Dependency Direction
```
adapter/in → port/in → service → port/out → adapter/out
```

- `adapter/in` depends on `port/in`
- `service` implements `port/in`, depends on `port/out`
- `adapter/out` implements `port/out`
- `domain` has no dependencies on any layer

## Layer Rules

### domain/
- Pure Kotlin class. No JPA annotations, no Spring annotations.
- Must not know about JPA, Redis, or any infrastructure.

### port/in/
- UseCase interface. Defines what the application can do.
- Called by `adapter/in` or web adapters.

### port/out/
- Persistence port interface. Defines what the application needs from outside.
- Called by `service`. Implemented by `adapter/out`.

### service/
- Implements UseCase. Contains business logic.
- Only knows `port/out` interfaces; never knows JPA directly.
- `@Transactional` goes here.

### adapter/in/ or adapter/web/
- GraphQL/Web adapter (`@MutationMapping`, `@QueryMapping`, or controller endpoint).
- Calls UseCase only. No business logic here.

### adapter/out/persistence/
- Implements persistence port.
- Knows JPA. Handles `{Domain}JpaEntity` ↔ `{Domain}` conversion via extension functions.
```kotlin
fun {Domain}JpaEntity.toDomain() = {Domain}(...)
fun {Domain}.toEntity() = {Domain}JpaEntity(...)
```

### global/
- Cross-cutting concerns: config, security, common exceptions.

## Naming Rules

| Layer | Naming |
|-------|--------|
| UseCase | `Fetch{Domain}UseCase`, `Append{Domain}UseCase` |
| PersistencePort | `{Domain}PersistencePort` |
| Service | `Fetch{Domain}Service`, `Modify{Domain}Service` |
| WebAdapter | `{Domain}WebAdapter` |
| PersistenceAdapter | `{Domain}PersistenceAdapter` |
| JpaEntity | `{Domain}JpaEntity` |
| JpaRepository | `{Domain}JpaRepository` |

## Developer-only APIs (`developer` domain)

The `developer` domain contains administrator-only operations that modify
**another member's** data. It is separate from the `member` domain, which
handles operations a member performs on their own data.

### Naming exception

In the naming table above, `{Domain}` normally matches the package name.
For `developer`, inbound ports and services are named after the **target**
being operated on, not the package.

| Layer | Correct | Wrong |
|-------|---------|-------|
| UseCase | `ModifyMemberSchoolInfoUseCase` | `ModifyDeveloperUseCase` |
| Service | `ModifyMemberSchoolInfoService` | `ModifyDeveloperService` |
| WebAdapter | `DeveloperWebAdapter` | `MemberWebAdapter` |
| PersistencePort | `DeveloperPersistencePort` | `MemberPersistencePort` |

Rule of thumb: **ports/services facing inward use the target (`Member`);
adapters and outbound ports use the package (`Developer`).**

### Rules

- Verify the caller's role at the start of the service method.
  `UserRole.ROOT` is required; throw `GsmcException(ErrorCode.FORBIDDEN)` otherwise.
- Do **not** use the `My` keyword. `SecurityContextHolder` identifies the
  *caller* here, not the target. `My` is reserved for self-service operations.
- Look up the target explicitly and throw `GsmcException(ErrorCode.USER_NOT_FOUND)`
  when absent. Unhandled exceptions fall through `GsmcExceptionResolver` as 500.
- Validate uniqueness by querying first. Do not rely on DB unique constraints
  alone — `DataIntegrityViolationException` is not a `GsmcException`.

