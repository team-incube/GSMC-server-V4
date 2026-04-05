---
name: code-review
description: Run a structured checklist over changed files — naming conventions, DTO, Kotlin style, JPA/transaction correctness, GraphQL schema, and security basics. Produces a ✓/⚠/✗ report.
allowed-tools: Bash
---

# Code Review Guide

Analyze changed files and verify the following items.

## Check Changes

1. Run `git diff` or `git diff develop...HEAD` to see changed files
2. Read each file for detailed analysis

## Checklist

### Naming
- [ ] camelCase used for packages, classes, interfaces, variables, and methods?
- [ ] snake_case used for `@Table` and DB-related parts?
- [ ] Table name follows `xxxx_tb` convention and is singular?
- [ ] Entity named correctly? (e.g. `MemberJpaEntity`, `BlackListRedisRepository`)
- [ ] Service class named with correct keyword? (`Fetch` / `Search` / `Modify` / `Append` / `Remove`)
- [ ] `My` keyword used when accessing `SecurityContextHolder`?

### DTO
- [ ] Request DTO name ends with `Query` or `Input`? (e.g. `GetUserQuery`, `CreateUserInput`)
- [ ] Response DTO name ends with `Payload` or `MutationPayload`? (e.g. `UserPayload`, `UserMutationPayload`)
- [ ] Controller parameter named `input`?

### GraphQL Schema
- [ ] Schema comments (`#`) written for all types, fields, and inputs?
- [ ] `!` (non-null) applied appropriately?
- [ ] `input` type used for Mutation arguments?

### Kotlin Style
- [ ] Using `val` vs `var` appropriately?
- [ ] Using constructor injection?
- [ ] Handling null safety properly?
- [ ] KtLint formatting applied?

### JPA / Transaction
- [ ] `@Transactional` opened in service layer, not repository layer?
- [ ] `@Transactional(readOnly = true)` applied for read operations?
- [ ] No N+1 problem? (Fetch Join needed?)

### Commit
- [ ] Following commit message convention? (`type(scope): description`)
- [ ] Using domain name for scope?
- [ ] Commits split into logical units?

### Security
- [ ] No hardcoded secrets?
- [ ] No sensitive information in logs?

## Report Format

For each item:
- ✓ Pass
- ⚠ Warning (recommendation)
- ✗ Error (needs fix)

Final summary:
- Total {n} items verified
- {p} passed, {w} warnings, {e} errors