---
name: architecture
description: Hexagonal architecture structure and rules for this project. Use this when creating new files or features to place them in the correct layer.
---

## Package Structure
```
src/main/kotlin/com/team/incube/gsmc/
├── domain/
│   └── {domain}/
│       ├── application/
│       │   ├── port/
│       │   │   ├── in/
│       │   │   │   └── Fetch{Domain}UseCase.kt
│       │   │   └── out/
│       │   │       └── {Domain}PersistencePort.kt
│       │   └── service/
│       │       └── Fetch{Domain}Service.kt
│       ├── domain/
│       │   └── {Domain}.kt
│       ├── exception/
│       │   └── {Domain}NotFoundException.kt
│       └── adapter/
│           ├── in/
│           │   └── {Domain}WebAdapter.kt
│           └── out/
│               └── persistence/
│                   ├── entity/
│                   │   └── {Domain}JpaEntity.kt
│                   ├── repository/
│                   │   └── {Domain}JpaRepository.kt
│                   └── {Domain}PersistenceAdapter.kt
├── global/
│   ├── config/
│   ├── exception/
│   └── security/
└── GsmcApplication.kt
```

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

### application/port/in/
- UseCase interface. Defines what the application can do.
- Called by `adapter/in`.

### application/port/out/
- Persistence port interface. Defines what the application needs from outside.
- Called by `service`. Implemented by `adapter/out`.

### application/service/
- Implements UseCase. Contains business logic.
- Only knows `port/out` interface — never knows JPA directly.
- `@Transactional` goes here.

### adapter/in/
- GraphQL controller (`@MutationMapping`, `@QueryMapping`).
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