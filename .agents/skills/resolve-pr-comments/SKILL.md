---
name: resolve-pr-comments
description: For each inline PR review comment, judge whether it has been resolved in the current branch diff and reply with the resolving commit hash. Use after addressing PR feedback.
---

## Step 1 - Collect PR Data

```bash
bash "scripts/get-pr-data.sh"
```

Then read:
- `.pr-tmp/pr_comments.json`
- `.pr-tmp/pr_changed_files.txt`
- `.pr-tmp/pr_commits.txt`
- `.pr-tmp/pr_diff.txt`

Also fetch repo and PR metadata:

```bash
gh repo view --json nameWithOwner -q .nameWithOwner
gh pr view --json number -q .number
```

## Step 2 - Judge Each Comment

For each comment in `pr_comments.json`, compare `path` + `body` against `pr_diff.txt`:

- **RESOLVED**: the diff contains a change in the same file that directly addresses the concern
- **UNRESOLVED**: no relevant change found for that file/concern

## Step 3 - Find Commit Hash

For resolved comments:

```bash
git log origin/<base>..HEAD --follow --pretty="%H %h %s" -- "<path>"
```

Select the 7-character short hash of the most relevant commit.

## Step 4 - Post Reply

Always quote variables to prevent shell injection. `path` and `comment_id` come from external PR data and may contain special characters.

```bash
gh api "repos/<owner>/<repo>/pulls/<pr_number>/comments/<comment_id>/replies" \
  -f body="<short_hash> 에서 해결했습니다."
```

## Step 5 - Report

```markdown
## 답변 완료 코멘트
- [file:line] "comment excerpt" -> {hash} 해결했습니다.

## 미해결 코멘트 (reply 없음)
- [file:line] "comment excerpt" -> 사유: ...
```

## Step 6 - Cleanup

Delete `.pr-tmp` after reporting.
