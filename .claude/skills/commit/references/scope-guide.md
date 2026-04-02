# Scope Selection Guide

**Priority**: Domain name > Module name

## Domain Names (Primary)

| Scope | Description |
|-------|-------------|
| auth | Authentication / API keys |
| account | Account management |
| student | Student information |
| club | Club / project information |
| neis | NEIS integration |
| client | OAuth client |
| oauth | OAuth2 flow |

## Module Names (Cross-cutting concerns only)

| Scope | Description |
|-------|-------------|
| global | Affects multiple modules |
| ci/cd | Build / deployment |
| web / openapi | Module-wide impact |

## Examples

**Wrong:**
- `fix(web): API 키 삭제 버그 수정` → `fix(auth): API 키 삭제 버그 수정`
- `refactor(common): 학생 엔티티 수정` → `refactor(student): 엔티티 필드 추가`

**Correct:**
- `refactor(global): 공통 예외 처리 로직 개선`
- `feat(ci/cd): GitHub Actions 워크플로우 추가`