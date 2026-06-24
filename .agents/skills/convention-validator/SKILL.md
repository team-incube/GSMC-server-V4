---
name: convention-validator
description: Detect and auto-fix Kotlin convention violations in changed files, using .claude/rules/convention.md and CLAUDE.md as the rule sources.
---

You are a Kotlin/Spring Boot convention enforcement skill for the GSMC-server-V4 project. Detect and fix convention violations in changed production Kotlin files, then report what changed.

## Step 1: Collect Changed Files

```bash
git status --short
git diff HEAD --name-only --diff-filter=ACMR
```

Keep only production `.kt` files. If no Kotlin files are changed, report that there is nothing to check and exit.

## Step 2: Load Rules

Read these files in order:

```bash
.claude/rules/convention.md
CLAUDE.md
```

Priority when rules conflict: `CLAUDE.md` > `.claude/rules/convention.md`.

## Step 3: Fix Violations

Check:
- Service keyword naming: `Fetch`, `Search`, `Modify`, `Append`, `Remove`
- Entity suffix: `JpaEntity`
- Request DTO suffix: `Query` or `Input`
- Response DTO suffix: `Payload` or `MutationPayload`
- GraphQL/web adapter argument name: `input`
- `@Transactional` only in service layer
- Prefer `val` over `var` where safe
- Constructor injection

If a fix would change business logic, report it under manual review instead of editing.

After edits, run:

```bash
./gradlew ktlintFormat
```

## Step 4: Output Report

```markdown
## Convention Validation Report

### Fixed Files
- <file>: <what was fixed>

### Requires Manual Review
- <file>: <reason>

### No Violations
- <file>
```
