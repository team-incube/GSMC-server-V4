---
name: pr-draft
description: Generate PR title, body, and labels from commits since the base branch, then create the PR on GitHub.
allowed-tools: Bash(git *:*), Bash(bash *create-pr.sh:*), Bash(cat *:*), Read, Write
---

## Step 1 — Gather Context
```bash
git branch --show-current
git log origin/develop..HEAD --oneline 2>/dev/null || git log --oneline -15
git diff origin/develop...HEAD --stat 2>/dev/null || git diff HEAD~5...HEAD --stat
git diff origin/develop...HEAD 2>/dev/null || git diff HEAD~5...HEAD
```

Also read the PR template:
```bash
cat .github/PULL_REQUEST_TEMPLATE.md
```

## Step 2 — Find Related Issue

Search GitHub issues to find the issue related to this PR:
```bash
gh issue list --limit 50 --state open
```

Match by:
1. Branch name keywords (e.g. `fix/hook` → look for issues about "hook")
2. Commit message keywords
3. Changed file names or domains

If a match is found, note the issue number for use in `Close #N`.
If no match is found, leave `Close #` blank.

Also check the issue title — **use it to align the PR title**.

## Step 3 — Determine Labels

Read `${CLAUDE_SKILL_DIR}/references/labels.md` and select 1 appropriate label.

> Note: `Type: Bug/UI` is issue-only. Use `Type: Bug/Function` for bug-fix PRs.

## Step 4 — Generate PR Content

**Title** — Generate 3 options in the format `[scope] description`:
- Scope: domain name (`[member]`, `[auth]`, `[score]`, etc.) or `[global]` / `[ci/cd]` for cross-cutting changes
- Description: Korean, concise, no period, max 50 characters total
- If a related issue was found, align the best option's description with the issue title
- Mark the best option with `← 추천`

**Body** — Follow `.github/PULL_REQUEST_TEMPLATE.md` structure:
- Write all content in Korean
- Style: `~하였습니다`, `~되었습니다`, `~추가하였습니다`
- No emojis, max 2500 characters
- Auto-check checklist items based on the nature of changes
```markdown
## 개요
<!-- 무엇을, 왜 변경했는지 작성해주세요 -->

## 변경 사항
<!-- 구체적인 변경 내용을 작성해주세요 -->

## 참고 사항
<!-- 리뷰어에게 전달할 내용이 있다면 작성해주세요 (선택) -->

## 체크리스트
- [ ] 이 작업으로 인해 변경이 필요한 문서를 작성 또는 수정했나요?
- [ ] 작업한 코드가 정상적으로 동작하는지 확인했나요?
- [ ] 작업한 코드에 대한 테스트 코드를 작성하거나 수정했나요?
- [ ] Merge 대상 브랜치를 올바르게 설정했나요?
- [ ] 해당 PR과 관련 없는 작업이 포함되지는 않았나요?
- [ ] PR의 올바른 라벨과 리뷰어를 설정했나요?

### 관련 이슈
- Close #N  <!-- replace N with issue number, or remove line if none -->
```

## Step 5 — Write Body & Show Preview

Write the body to `PR_BODY.md`, then display:
