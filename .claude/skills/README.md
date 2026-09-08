# Skills

Claude에서 `/skill-name`으로 호출하거나, 관련 작업 시 자동으로 참조되는 재사용 가능한 작업 단위입니다.

## Git 워크플로우

| 스킬 | 설명 |
|------|------|
| [commit](./commit/SKILL.md) | 변경사항을 논리적 단위로 분리해 컨벤션에 맞는 커밋 생성. Git Flow 자동 감지(develop 브랜치 체크) |
| [pr-draft](./pr-draft/SKILL.md) | base 브랜치 이후 커밋 기반으로 PR 제목·본문·라벨 생성 및 GitHub PR 오픈까지 자동화 |
| [resolve-pr-comments](./resolve-pr-comments/SKILL.md) | PR 리뷰 코멘트 수집 후 컨벤션 기준으로 판단 — 유효하면 자동 반영, 무효하면 반박 댓글 작성 |

## 코드 품질 / 보안

| 스킬 | 설명 |
|------|------|
| [code-review](./code-review/SKILL.md) | 네이밍·DTO·Kotlin 스타일·JPA/트랜잭션·GraphQL 스키마·보안 기본값을 체크리스트로 검사. ✓/⚠/✗ 리포트 출력 |
| [security-checklist](./security-checklist/SKILL.md) | 하드코딩 시크릿·SQL 인젝션·JWT/OAuth 검증·민감 로깅·인가 검사. auth·security 관련 PR 머지 전 필수 |

## 설계 및 아키텍처

| 스킬 | 설명 |
|------|------|
| [architecture](./architecture/SKILL.md) | 헥사고날 아키텍처 계층 책임(adapter/in → port/in → service → port/out → adapter/out), 네이밍 규칙, developer 도메인 예외 규칙 |
| [the-sdk](./the-sdk/SKILL.md) | the-sdk 공통 라이브러리 사용 가이드 — 이 프로젝트에서 켜져 있는 기능(logging)과 꺼져 있는 기능(response/swagger/exception) 구분 |
| [database-schema](./database-schema/SKILL.md) | 테이블/컬럼 네이밍(xxxx_tb 단수), 인덱스 전략, Flyway 마이그레이션, JPA Entity 매핑 패턴 |
| [migration-guide](./migration-guide/SKILL.md) | DB 스키마 변경·Entity 수정 영향 분석, 헥사고날 계층 기준 올바른 변경 순서, 2단계 컬럼 삭제 |
| [docker](./docker/SKILL.md) | Dockerfile / docker-compose.yml 작성 가이드 — 멀티스테이지 빌드, 레이어 캐싱, 시크릿 미포함 원칙 |
| [planning](./planning/SKILL.md) | 숨겨진 요구사항·트레이드오프·제약을 발굴하는 구조화된 인터뷰 후 상세 구현 스펙 파일 생성 |

## 테스트

| 스킬 | 설명 |
|------|------|
| [kotest-guide](./kotest-guide/SKILL.md) | Kotest(BehaviorSpec) + MockK 패턴 — Given/When/Then 구조, mock 생성, GsmcException 검증 |
