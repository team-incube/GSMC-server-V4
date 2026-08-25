---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically - detects develop branch and checks out a feature branch before committing.
---

## Step 0 - Branch Check (Required)

Check the current branch first:

```bash
git branch --show-current
```

**If current branch is `develop`:**

This project uses Git Flow. Do NOT commit directly to `develop`.

1. Inspect all changes: `git status`, `git diff`
2. Plan every commit before touching git - determine `type`, `scope`, and `description` for each changed file following the Commit Message Rules below
3. Derive the branch name from the **primary commit** (the most significant change):
    - Format: `<type>/<scope>-<kebab-case-description>`
    - Exception: if scope is `ci/cd`, use `cicd/` as the branch prefix instead
    - Examples:
        - Primary commit `feat(member): 프로필 조회 API 추가` -> `feat/member-add-profile-api`
        - Primary commit `fix(auth): 토큰 만료 버그 수정` -> `fix/auth-token-expiry-bug`
        - Primary commit `refactor(score): 쿼리 최적화` -> `refactor/score-optimize-query`
        - Primary commit `feat(ci/cd): GitHub Actions 워크플로우 추가` -> `cicd/add-github-actions-workflow`
4. Create and checkout the branch:
   ```bash
   git checkout -b <derived-branch-name>
   ```
5. Proceed with the commit flow below

**If current branch is NOT `develop`:** proceed directly to the commit flow.

---

## Commit Message Rules

Format: `type(scope): description`

- **Types**: `feat` / `refactor` / `fix` / `delete` / `docs` / `test` / `merge` / `init`
- **Scope**: domain name by default - for the full selection table, read `.agents/skills/commit/references/scope-guide.md`
- **Description**: Korean, no period, avoid noun-ending style and endings: `~한다/~된다`, `~하기`, `~합니다/~됩니다`, `~했습니다`
    - Good: `엔티티 필드 추가`, `트랜잭션 롤백 방지`, `로직 개선`
- Subject line only (no body)
- Do NOT add AI as co-author

## Commit Flow

1. Inspect changes: `git status`, `git diff`
2. Group changed files into logical units - files that belong to the same concern go into one commit:
    - e.g. new feature's UseCase + Service + Adapter -> one `feat` commit
    - e.g. ktlint formatting across multiple files -> one `refactor` commit
    - e.g. changes in unrelated domains -> separate commits
3. For each logical group:
    - Stage all files in the group: `git add <file1> <file2> ...`
    - Write one commit message that describes the group's intent
    - `git commit -m "message"`
4. Verify with `git log --oneline -n <count>`

> **Rule**: Changes with the same logical purpose go into one commit. Changes with different purposes must be separated.
