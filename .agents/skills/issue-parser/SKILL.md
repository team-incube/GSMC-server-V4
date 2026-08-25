---
name: issue-parser
description: Parse a GitHub issue into an implementation brief, suggested branch name, commit scope, affected domains, and acceptance checklist.
---

Use this skill when the user provides a GitHub issue number, URL, or asks to start work from an issue.

## Step 1 - Load Issue

If the user gives an issue number or URL, fetch it with GitHub CLI:

```bash
gh issue view <issue> --json number,title,body,labels,assignees,state,url
```

If GitHub CLI is unavailable, ask the user for the issue body.

## Step 2 - Parse Work Scope

Extract:
- Problem statement
- Requested behavior
- Affected domain (`auth`, `score`, `member`, `global`, `ci/cd`, etc.)
- Likely changed layers based on the architecture skill
- Explicit requirements
- Ambiguous or missing requirements
- Acceptance criteria
- Test expectations

## Step 3 - Suggest Git Flow Metadata

Suggest:
- Branch name: `<type>/<scope>-<kebab-case-description>`
- Commit type: `feat`, `fix`, `refactor`, `docs`, `test`, `delete`, `merge`, or `init`
- Commit scope: use `commit/references/scope-guide.md` when needed
- PR title: `[scope] Korean description`

Do not create the branch unless the user asks or the task clearly requires implementation immediately.

## Step 4 - Output

```markdown
## Issue Brief

### Summary
<one or two sentences>

### Parsed Scope
- Domain:
- Type:
- Suggested branch:
- Suggested commit:

### Requirements
- ...

### Acceptance Checklist
- [ ] ...

### Open Questions
- ...

### Implementation Notes
- ...
```
