# 프로젝트 참여 점수 신청 기능 — DataGSM 연동 구현 계획

- 작성일: 2026-07-09
- 관련 문서: [`score-add-api-plan.md`](./score-add-api-plan.md) (범용 점수 추가/승인/거절/삭제, 이 문서와
  같은 `score.graphqls`/`ScoreWebAdapter`를 공유한다 — 그 문서의 `type Mutation` 선언에 이어서 이 문서의
  뮤테이션을 `extend`로 추가)

## 1. 개요

`PROJECT_PARTICIPATION`(프로젝트 참여) 카테고리는 학교 공식 시스템인 **DataGSM**에 이미 등록된 프로젝트와
참여자 명단을 그대로 활용한다. 여러 차례 설계를 다시 그렸는데(팀장이 프로젝트/참여자/사진을 한 번에
입력하는 단일 뮤테이션 → 팀장이 프로젝트를 만들고 참여자를 관리하는 `Project` 엔티티 기반 설계), 최종적으로
**GSMC가 팀/참여자 관리 기능을 따로 만들 필요가 없다**는 결론에 도달했다. 참여자 각자가 로그인해서 "내가
참여한 DG 프로젝트 목록"을 보고, 그 중 하나를 골라 개인적으로 독립 제출하면 충분하다.

핵심 배경:
- DataGSM OpenAPI(`https://openapi.datagsm.kr`, `X-API-KEY` 헤더 인증)는 기존 `datagsm-oauth-sdk-java`
  의존성(OAuth 전용)과는 무관한 별도의 순수 REST API다. `GET /v1/projects`에서 프로젝트명/설명/참여자
  (이름·이메일·학번·전공)를 받아올 수 있지만 역할/사진/팀장 여부는 제공하지 않는다.
- `domain/project` 폴더는 아직 없고, `project.graphqls`에는 구현되지 않은 죽은 스캐폴딩
  (`CreateProjectInput`, `PatchProjectInput`, `CreateProjectDraftInput`)만 남아 있다 — 이번에 전체 교체한다.
- "제출은 개인 단위"라는 점에서 `score-add-api-plan.md`의 4개 범용 add 뮤테이션과 본질적으로 같은 패턴이다.
  차이는 DataGSM 재검증과 여러 장 사진 첨부, "같은 프로젝트로 제출한 사람 모아보기" 조회가 추가된다는 것.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| "작성 가능한 프로젝트 목록" 조회 범위 | 로그인한 학생의 이메일이 DataGSM 프로젝트 `participants[].email`에 포함된 `status=ACTIVE` 프로젝트만. GSMC DB에 쓰기 없음, 점수 없음(순수 조회) |
| 팀/리더/참여자 관리 | **없음** — `Project`/`ProjectParticipant` 엔티티, "팀장" 개념, 참여자 추가/삭제 기능을 만들지 않음 |
| 제출 방식 | 참여자 각자 로그인해서 **개인별로 독립 제출** (기여 내용 텍스트 + 사진 여러 장). 제출 시점에 DataGSM 재조회로 본인이 실제 참여자인지 확인 |
| 같은 프로젝트 모아보기(교사용) | 신규 조회 `scoresByDgProjectId(dgProjectId)`로 같은 프로젝트에 제출한 사람 전원의 `Score`를 한 번에 조회 |
| 승인/거절/삭제 | `score-add-api-plan.md`에 이미 설계된 `approveScore`/`rejectScore`/`deleteScore`를 참여자별 `Score` row 단위로 그대로 재사용 (팀 일괄 승인 API는 범위 밖) |
| GSMC User 자동 생성 | **불필요** — 다른 사람을 대신해 계정을 만들 상황이 없음(각자 로그인해서 각자 제출) |
| `ScoreStatus.INCOMPLETE` | **미사용** — 제출 자체가 최초 액션이므로 범용 add 뮤테이션과 동일하게 바로 `PENDING`으로 생성 |
| 신규 GSMC 엔티티 | 없음 — `Score`에 `dgProjectId: Long?` 컬럼 1개만 추가 |
| DataGSM 연동 위치 | 경량 `domain/project` — DataGSM OpenAPI 조회 전용(조회 Query 1개 + 포트/어댑터). 실제 저장/제출 로직은 전부 `domain/score` |
| 중복 제출 방지 | `(userId, dgProjectId)` 조합으로 기존 `Score` 조회 → `PENDING`/`APPROVED`면 차단, `REJECTED`면 재사용(`score-add-api-plan.md` §8.4 패턴을 dgProjectId 스코프로 확장) |
| "2인 이상 협업" 검증 | GSMC가 참여자 수를 세는 게 아니라, 제출 시점에 재조회한 **DG 프로젝트의 `participants.size >= 2`**로 검증 |
| 개인 기여 제출 표현 | 제출자 1인당 `Evidence` 1개(본인 소유) + 그 Evidence에 `File`(사진) N개 연결 — 기존 스키마 재사용 |
| 프로젝트명 노출 | `Score.activityName = DG 프로젝트명 스냅샷` (`PROJECT_PARTICIPATION`은 COUNT_BASED이므로 §8.2 규칙과 일치) |

## 3. 데이터 모델

**`score_tb` 변경**: `dg_project_id`(BIGINT, NULLABLE) 컬럼 1개 추가. `PROJECT_PARTICIPATION`이 아닌
카테고리의 row는 항상 null. 신규 테이블 없음.

**Evidence/File 재사용**: 최초 제출 시 `Evidence(evidenceTitle=dgProject.name, evidenceContent=input.content,
user=제출자)` 신규 저장 → `input.fileIds`를 그 Evidence에 연결. `REJECTED` 재제출 시 기존 Evidence의
`evidenceContent`만 갈아끼우고, 파일은 전체 교체(새 목록에 없는 기존 연결은 해제, 새로 지정된 파일은 연결).

## 4. 신규/변경 파일

### `domain/project` (신규, 경량 — DataGSM 조회 게이트웨이 전용)

```
domain/project/DataGsmProject.kt, DataGsmProjectParticipant.kt, DataGsmProjectStatus.kt, DataGsmClub.kt   — 순수 도메인 모델

domain/project/port/out/DataGsmProjectApiPort.kt
  — findActiveProjectsByParticipantEmail(email: String): List<DataGsmProject>
  — findProjectById(dgProjectId: Long): DataGsmProject?

domain/project/port/in/FetchMyWritableDataGsmProjectsUseCase.kt
domain/project/service/FetchMyWritableDataGsmProjectsService.kt
domain/project/adapter/web/ProjectWebAdapter.kt        — @QueryMapping 1개(myWritableDataGsmProjects)

domain/project/adapter/out/openapi/
  DataGsmOpenApiProperties.kt   — @ConfigurationProperties(prefix="datagsm.openapi")
  DataGsmOpenApiConfig.kt       — RestClient Bean (baseUrl + X-API-KEY 헤더)
  DataGsmProjectApiAdapter.kt   — GET /v1/projects 호출 + 이메일 클라이언트측 필터링(목록 조회), 단건 재조회
  dto/ (DataGsmProjectListResponseDto 등 Jackson 매핑 DTO + toDomain() 확장함수)
```

### `domain/score` (변경 + 신규)

- `Score.kt` — `dgProjectId: Long?` 필드 추가
- `adapter/out/persistence/entity/ScoreJpaEntity.kt` — `dg_project_id` 컬럼 추가 (nullable)
- `adapter/out/persistence/entity/ScoreJpaEntityExtensions.kt` — 매핑 추가
- `port/out/ScorePersistencePort.kt` — `findByUserIdAndDgProjectId(userId, dgProjectId): Score?`,
  `findAllByDgProjectId(dgProjectId): List<Score>` 추가 (`save`/`deleteById`는 `score-add-api-plan.md`에서
  이미 추가되므로 재사용)
- `adapter/out/persistence/ScorePersistenceAdapter.kt` — 위 두 메서드 구현
- `port/in/SubmitProjectParticipationUseCase.kt` (신규)
- `port/in/FetchScoresByDgProjectIdUseCase.kt` (신규, 교사용)
- `service/SubmitProjectParticipationService.kt` (신규) — `DataGsmProjectApiPort` 의존
- `service/FetchScoresByDgProjectIdService.kt` (신규)
- `adapter/web/ScoreWebAdapter.kt` — `@MutationMapping submitProjectParticipation`,
  `@QueryMapping scoresByDgProjectId` 추가

### 기타 변경

- `src/main/resources/graphql/project.graphqls` — 죽은 스캐폴딩 전체 삭제 후 §5 스키마로 교체
- `src/main/resources/graphql/score.graphqls` — `Score` 타입에 `dgProjectId: ID` 필드 추가,
  `SubmitProjectParticipationInput` 추가, `extend type Mutation`에 `submitProjectParticipation`,
  `extend type Query`에 `scoresByDgProjectId` 추가 (base `type Mutation`/`type Query`는
  `score-add-api-plan.md` 작업으로 이 파일에 이미 선언되어 있음)
- `global/config/PropertyScanConfig.kt` — `@EnableConfigurationProperties`에 `DataGsmOpenApiProperties::class` 추가
- `global/exception/ErrorCode.kt` — §6 신규 코드 추가
- `src/main/resources/application.yaml` — `datagsm.openapi.base-url`/`api-key` 설정 추가

## 5. GraphQL 스키마

**`project.graphqls` (전체 교체)**
```graphql
type DataGsmClub {
    id: ID!
    name: String!
    type: String
}

type DataGsmProjectParticipant {
    id: ID!
    name: String!
    email: String!
    studentNumber: String
    major: String
    sex: String
}

enum DataGsmProjectStatus {
    ACTIVE
    ENDED
}

type DataGsmProject {
    dgProjectId: ID!
    name: String!
    description: String
    startYear: Int
    endYear: Int
    status: DataGsmProjectStatus!
    club: DataGsmClub
    participants: [DataGsmProjectParticipant!]!
}

extend type Query {
    myWritableDataGsmProjects: [DataGsmProject!]!
}
```

**`score.graphqls` 추가분**
```graphql
input SubmitProjectParticipationInput {
    content: String!
    fileIds: [ID!]!
}

# Score 타입에 필드 추가: dgProjectId: ID

extend type Query {
    scoresByDgProjectId(dgProjectId: ID!): [Score!]!
}

extend type Mutation {
    submitProjectParticipation(dgProjectId: ID!, input: SubmitProjectParticipationInput!): Score!
}
```

## 6. 신규 `ErrorCode`

| 코드 | HTTP | 설명 |
| --- | --- | --- |
| `DATAGSM_PROJECT_NOT_FOUND` | 404 | DataGSM 프로젝트를 찾을 수 없음 |
| `DATAGSM_PROJECT_NOT_ACTIVE` | 400 | 종료된 프로젝트는 제출 불가 |
| `DATAGSM_API_CALL_FAILED` | 502 | DataGSM 연동 실패 |
| `NOT_A_DATAGSM_PROJECT_PARTICIPANT` | 400 | 해당 프로젝트의 DataGSM 참여자가 아님 |
| `INVALID_PROJECT_PARTICIPANT_COUNT` | 400 | DG 프로젝트 참여자가 2인 미만 |
| `PROJECT_PARTICIPATION_ALREADY_SUBMITTED` | 409 | 이미 제출/심사 중인 프로젝트(`PENDING`/`APPROVED` 상태) |
| (재사용) `FILE_NOT_FOUND`, `FORBIDDEN`, `USER_NOT_FOUND` | — | 기존/`score-add-api-plan.md` 재사용 |

## 7. 처리 흐름

1. **`myWritableDataGsmProjects`**: `STUDENT` 검증 → 내 이메일 확보 → DataGSM `GET /v1/projects?status=ACTIVE`
   조회(필요시 페이지 순회) → `participants[].email`에 내 이메일이 포함된 것만 필터링 → 반환 (DB 쓰기 없음)
2. **`submitProjectParticipation(dgProjectId, {content, fileIds})`**:
   1. `STUDENT` 검증
   2. `DataGsmProjectApiPort.findProjectById(dgProjectId)` → 없으면 `DATAGSM_PROJECT_NOT_FOUND`,
      `status != ACTIVE`면 `DATAGSM_PROJECT_NOT_ACTIVE`
   3. `currentUser.email ∉ dgProject.participants.email` → `NOT_A_DATAGSM_PROJECT_PARTICIPANT`
   4. `dgProject.participants.size < 2` → `INVALID_PROJECT_PARTICIPANT_COUNT`
   5. `ScorePersistencePort.findByUserIdAndDgProjectId(currentUserId, dgProjectId)` 조회
      - 있고 `scoreStatus ∈ {PENDING, APPROVED}` → `PROJECT_PARTICIPATION_ALREADY_SUBMITTED`
      - 있고 `REJECTED` → 그 row의 `Evidence` 내용만 갈아끼우고 파일 전체 교체, `rejectionReason=null`,
        `scoreStatus=PENDING`
      - 없음 → `fileIds` 존재/소유자 검증 → `Evidence` 신규 저장 → 파일 연결 →
        `Score(category=PROJECT_PARTICIPATION, dgProjectId, evidence, activityName=dgProject.name, scoreStatus=PENDING)`
        신규 저장
   6. 저장된 `Score` 반환
3. **`scoresByDgProjectId(dgProjectId)`**: `isTeacherOrAbove()` 아니면 `FORBIDDEN` →
   `ScorePersistencePort.findAllByDgProjectId(dgProjectId)` 반환 — 각자 `scoreId`로
   `approveScore`/`rejectScore` 개별 호출
4. **승인/거절/삭제**: `score-add-api-plan.md`의 `approveScore`/`rejectScore`/`deleteScore`를 그대로 사용

## 8. 열린 이슈 (의도적으로 범위 밖으로 둔 것)

- `myWritableDataGsmProjects`가 매 호출마다 DG 전체 ACTIVE 프로젝트를 순회+이메일 필터링 — 프로젝트 수가
  늘면 비효율적, 캐싱은 후속 과제
- 같은 프로젝트에 참여한 팀원 중 한 명이 로그인하지 않으면 그 사람은 점수를 못 받음(개인별 독립 제출
  구조의 자연스러운 트레이드오프)
- 팀장/역할 구분, 팀 일괄 승인 API는 범위 밖

## 9. 작업 순서 (체크리스트)

1. [ ] `domain/project` 신규 생성 — 도메인 모델, `DataGsmProjectApiPort`, `DataGsmProjectApiAdapter`(RestClient),
   `DataGsmOpenApiProperties`/`Config`
2. [ ] `PropertyScanConfig`에 `DataGsmOpenApiProperties::class` 추가, `application.yaml`에 설정 추가
3. [ ] `FetchMyWritableDataGsmProjectsUseCase`/`Service`, `ProjectWebAdapter` 구현
4. [ ] `Score`/`ScoreJpaEntity`에 `dgProjectId` 추가, `ScorePersistencePort`에
   `findByUserIdAndDgProjectId`/`findAllByDgProjectId` 추가 및 어댑터 구현
5. [ ] `SubmitProjectParticipationUseCase`/`Service` 구현 (§7-2 로직)
6. [ ] `FetchScoresByDgProjectIdUseCase`/`Service` 구현
7. [ ] `ScoreWebAdapter`에 `submitProjectParticipation`/`scoresByDgProjectId` 추가
8. [ ] `project.graphqls` 전체 교체, `score.graphqls`에 관련 타입/필드 추가
9. [ ] `ErrorCode`에 §6 신규 코드 추가
10. [ ] KtLint 포맷 적용
11. [ ] GraphiQL로 목록 조회, 정상/에러 제출, 중복 제출 차단, REJECTED 재사용, `scoresByDgProjectId` 권한
    및 다인 조회 수동 검증 (`score-add-api-plan.md`가 선행되어 있어야 함 — `save`/`deleteById`,
    `FilePersistencePort`, `EvidencePersistencePort` 의존)
