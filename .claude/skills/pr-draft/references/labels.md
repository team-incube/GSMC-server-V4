# Labels Reference

Select **1 Type label** for PRs.

## Type Labels (pick 1)

| Label | Description | When to use |
|-------|-------------|-------------|
| `Type: Feature/Function` | 새로운 기능 및 개선 사항 | New feature, improvement, refactoring |
| `Type: Feature/Document` | 문서 추가 및 보완 작업 | Docs, README, skill files, comments |
| `Type: Bug/Function` | 정상 동작하지 않는 기능 | Functional bug fix |
| `Type: Release` | 릴리즈 | Release preparation, version bump |
| `Setting` | 환경 세팅 | Environment config, build, CI/CD |

## Off-limits Labels (do NOT assign)

`Priority: *`, `Status: *`, `Type: Bug/UI` are issue-only — never assign to PRs.

## Quick Decision

```
New feature or improvement? → Type: Feature/Function
Docs, README, skill files?  → Type: Feature/Document
Functional bug?             → Type: Bug/Function
Release?                    → Type: Release
CI/CD, build, env config?   → Setting
```
