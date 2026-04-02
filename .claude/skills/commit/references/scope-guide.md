# Scope Selection Guide

**Priority**: Domain name > Module name

## Domain Names (Primary)

| Scope | Description |
|-------|-------------|
| alert | Alert management |
| archive | Archive management |
| auth | Authentication / API keys |
| category | Category management |
| developer | Developer management |
| evidence | Evidence management |
| file | File management |
| member | Member management |
| project | Project management |
| score | Score management |
| sheet | Sheet management |

## Module Names (Cross-cutting concerns only)

| Scope | Description |
|-------|-------------|
| global | Affects multiple modules |
| ci/cd | Build / deployment |

## Examples

**Wrong:**
- `fix(global): 인증 토큰 삭제 버그 수정` → `fix(auth): 인증 토큰 삭제 버그 수정`
- `refactor(global): 멤버 엔티티 수정` → `refactor(member): 엔티티 필드 추가`

**Correct:**
- `refactor(global): 공통 예외 처리 로직 개선`
- `feat(ci/cd): GitHub Actions 워크플로우 추가`