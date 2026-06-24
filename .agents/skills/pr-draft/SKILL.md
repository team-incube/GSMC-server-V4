---
name: pr-draft
description: Generate PR title, body, and labels from commits since the base branch, then create the PR on GitHub.
---

## Step 1 - Gather Context

```bash
git branch --show-current
git log origin/develop..HEAD --oneline 2>/dev/null || git log --oneline -15
git diff origin/develop...HEAD --stat 2>/dev/null || git diff HEAD~5...HEAD --stat
git diff origin/develop...HEAD 2>/dev/null || git diff HEAD~5...HEAD
```

Also read `.github/PULL_REQUEST_TEMPLATE.md`.

## Step 2 - Find Related Issue

Search GitHub issues to find the issue related to this PR:

```bash
gh issue list --limit 50 --state open
```

Match by:
1. Branch name keywords
2. Commit message keywords
3. Changed file names or domains

If a match is found, note the issue number for `Close #N`. If no match is found, leave it blank.

## Step 3 - Determine Labels

Read `references/labels.md` in this skill directory and select 1 appropriate label.

## Step 4 - Generate PR Content

Title: Generate 3 options in the format `[scope] description`.

Body: Follow `.github/PULL_REQUEST_TEMPLATE.md`, write in Korean, keep it concise, and auto-check applicable checklist items.

## Step 5 - Write Body & Show Preview

Write the body to `PR_BODY.md`, then display the title options, selected label, and body preview.

Base branch is always `develop` for feature/fix/refactor branches. Always pass `--base develop` when running `gh pr create`.

## Step 6 - Create PR & Cleanup

After the user confirms and `gh pr create` succeeds, delete `PR_BODY.md`.
