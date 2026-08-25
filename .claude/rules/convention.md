# Code Convention

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

- e.g. RDB + user information → `MemberJpaEntity`
- e.g. Redis + blacklist → `BlackListRedisRepository`
- Repository method names must follow Spring Data naming conventions.

## Service

| Action | Keyword | Example |
|--------|---------|---------|
| Fetch | Fetch | `FetchMemberService` |
| Search | Search | `SearchProjectService` |
| Update | Modify | `ModifyMemberService` |
| Create | Append | `AppendScoreService` |
| Delete | Remove | `RemoveScoreService` |

- Use the `My` keyword when using `SecurityContextHolder`.
    - e.g. `FetchMyScoreService`, `ModifyMyCertificateService`
- If none of the 5 keywords apply, name it so that anyone can understand its purpose at a glance.

## Controller

Fix the DTO parameter name to `input` in the Presentation layer.

```kotlin
@MutationMapping
fun exampleMutation(@Argument input: ExampleInput): ExamplePayload {
    return exampleService.execute(input)
}
```

## Transaction

- Open transactions in the `service` layer only.
- Never open transactions in the `repository` layer.

## Formatting

- Always apply KtLint formatting before committing.
