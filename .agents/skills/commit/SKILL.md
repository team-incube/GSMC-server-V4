---
name: commit
description: Create Git commits by splitting changes into logical units following project conventions. Handles Git Flow automatically by avoiding direct commits on develop.
---

## Step 0 - Branch Check

Check the current branch first:

```bash
git branch --show-current
```

If the current branch is `develop`, do not commit directly.

1. Inspect all changes with `git status` and `git diff`.
2. Plan each logical commit before touching git.
3. Derive a branch name from the primary commit:
   - Format: `<type>/<scope>-<kebab-case-description>`
   - If scope is `ci/cd`, use `cicd/` as the branch prefix.
   - Examples:
     - `feat/member-add-profile-api`
     - `fix/auth-token-expiry-bug`
     - `refactor/score-optimize-query`
     - `cicd/add-github-actions-workflow`
4. Create and checkout the branch:

```bash
git checkout -b <derived-branch-name>
```

If the current branch is not `develop`, proceed directly to the commit flow.

## Commit Message Rules

Format:

```text
type(scope): description
```

- Types: `feat`, `refactor`, `fix`, `delete`, `docs`, `test`, `merge`, `init`
- Scope: domain name by default. Use `references/scope-guide.md` in this skill directory when unsure.
- Description: Korean, no period, concise action phrase.
- Subject line only. Do not add a body.
- Do not add AI as co-author.

Good examples:

```text
feat(auth): 로그인 토큰 발급 추가
fix(score): 점수 계산 오류 수정
refactor(global): 예외 처리 로직 개선
docs(global): Codex 하네스 문서 추가
```

## Commit Flow

1. Inspect changes: `git status`, `git diff`.
2. Group changed files into logical units. Files with the same purpose go into one commit.
3. Stage all files in the logical group:

```bash
git add <file1> <file2>
```

4. Commit with one message that describes the group's intent:

```bash
git commit -m "type(scope): description"
```

5. Repeat for unrelated groups.
6. Verify with `git log --oneline -n <count>`.

Rule: Changes with the same logical purpose go into one commit. Changes with different purposes must be separated.
