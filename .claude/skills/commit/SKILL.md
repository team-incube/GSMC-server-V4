---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically — detects develop branch and checks out a feature branch before committing.
allowed-tools: Bash
---

## Step 0 — Branch Check (Required)

Check the current branch first:
```bash
git branch --show-current
```

**If current branch is `develop`:**

This project uses Git Flow. Feature branches must be created from `develop` and merged back into `develop`.

1. Analyze all changes with `git status` and `git diff`
2. Infer an appropriate branch name from the changes:
    - Format: `<type>/<kebab-case-description>`
    - Reflect the domain scope in the name
    - Examples: `feat/add-student-major-filter`, `fix/auth-api-key-deletion`, `refactor/optimize-club-query`
3. Create and checkout the branch:
```bash
   git checkout -b <type>/<inferred-name>
```
4. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type(scope): description`

- **Types**: `feat` / `refactor` / `fix` / `delete` / `docs` / `test` / `merge` / `init`
- **Scope**: domain name by default — for the full selection table, read `${CLAUDE_SKILL_DIR}/references/scope-guide.md`
- **Description**: Korean, no period, avoid noun-ending style
    - Good: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)
- Do NOT add AI as co-author

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Categorize into logical units
3. Group files per unit
4. For each group:
    - Stage only relevant files with `git add`
    - Write a commit message following the rules above
    - `git commit -m "message"`
5. Verify with `git log --oneline -n <count>`