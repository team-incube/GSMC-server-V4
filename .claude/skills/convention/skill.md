---
name: convention
description: Code conventions for naming, DTO, Entity, Repository, Service, and Controller in this project.
---

## Naming

- Use camelCase for packages, classes, interfaces, variables, and methods.
- Use snake_case for DB-related parts (e.g. `@Table` annotation).
- Table names follow the `xxxx_tb` convention and must be singular.

## DTO

### Request DTO

| Action | Naming |
|--------|--------|
| Fetch | `GetUserQuery` |
| Search | `SearchUserQuery` |
| Create | `CreateUserInput` |
| Update | `UpdateUserInput` |
| Delete | `DeleteUserInput` |

### Response DTO

| Action | Naming |
|--------|--------|
| Fetch / Search | `UserPayload` |
| Create / Update / Delete | `UserMutationPayload` |

> e.g. Response DTO for fetching certifications → `CertificationPayload`

## Entity / Repository

- Name Entity and Repository based on the target database.
    - e.g. RDB + user information → `MemberJpaEntity`
    - e.g. Redis + blacklist → `BlackListRedisRepository`
- Repository method names must follow Spring Data naming conventions.

## Service

- Name service classes based on the action they perform.

| Action | Keyword |
|--------|---------|
| Fetch | `Fetch` |
| Search | `Search` |
| Update | `Modify` |
| Create | `Append` |
| Delete | `Remove` |

- Use the `My` keyword when using `SecurityContextHolder`.
    - e.g. Fetch a user's certification by email → `FetchCertificateByEmailService`
    - e.g. Modify the authenticated user's certification → `ModifyMyCertificateService`
- If none of the 5 keywords apply, name it so that anyone can understand its purpose at a glance.

## Controller

- Fix the DTO parameter name to `input` in the Presentation layer.
```kotlin
@MutationMapping
fun exampleMutation(@Argument input: ExampleInput): ExamplePayload {
    return exampleService.execute(input)
}
```

## Other

- Open transactions in the service layer, not in the repository layer.
- Document thoroughly using GraphiQL and schema comments (`#`).
- Always apply KtLint formatting.