# Score 도메인 점수 추가/승인/거절/삭제 GraphQL Mutation API 구현 계획

- 작성일: 2026-07-09

## 1. 개요

학생이 카테고리별로 점수(자격증/시험/봉사/수상 등)를 신청하는 4종의 범용 "추가" 뮤테이션과, 교사·관리자가
그 신청을 승인/거절/삭제하는 3종의 뮤테이션, 총 7종의 GraphQL Mutation을 구현한다.

`PROJECT_PARTICIPATION`(프로젝트 참여) 카테고리는 이 문서에서 범용 뮤테이션 대상으로 다루지 않는다 —
DataGSM 연동을 포함한 별도 설계로 분리되어 [`project-participation-plan.md`](./project-participation-plan.md)에서 다룬다.

`score` 도메인에는 현재 Query 8종(`FetchXxxService`)만 구현되어 있고, **프로젝트 전체에 `type Mutation`이
단 하나도 정의되어 있지 않다** — 이번 작업이 이 프로젝트의 **첫 GraphQL Mutation 구현**이다. 또한
`category`/`evidence`/`file` 도메인은 도메인 모델과 JPA 엔티티만 있고 `port`/`service`/`adapter`가
전혀 없어, 이번 작업에서 최소한의 조회 포트를 새로 만들어야 한다.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| categoryType 검증 방식 | 하드코딩된 목록이 아니라, `categoryType`으로 `Category`를 조회해 **`evidenceType`/`calculationType` 값**으로 동적 검증 (§8.1) |
| 기존 카테고리 재정의 범위 | `VOLUNTEER`(FILE→UNREQUIRED), `TOEIC_ACADEMY`(EVIDENCE→UNREQUIRED), `READ_A_THON`(EVIDENCE→FILE) — 이번 작업 범위에 포함해 문서 갱신 (실 운영 DB `category_tb` 값 변경은 배포 시 수동 반영 필요) |
| 신규 카테고리 `NCS`, `NEWRROW_SCHOOL` | `CategoryType` enum 값만 추가. `category_tb` row(weight/최대점수/isAccumulated 등) 설정은 범위 밖 — 관리자가 추후 수동 반영 |
| `NCS`(직업기초능력평가) 분류 | 숫자만 입력받고 파일 첨부 없음 → `evidenceType=UNREQUIRED`, `calculationType=SCORE_BASED`로 분류, `addScoreWithValue` 버킷 (최초 스펙 문서의 `addScoreWithFile` 그룹 배정을 정정) |
| 프로젝트 참여(`PROJECT_PARTICIPATION`) | 범용 4개 뮤테이션 대상에서 제외, DataGSM 연동 전용 뮤테이션(`submitProjectParticipation`)으로 분리 — [`project-participation-plan.md`](./project-participation-plan.md) 참고 |
| `value` → `scoreValue`/`activityName` 매핑 | §8.2 규칙(계산 방식 기준 분기)으로 원시값만 저장. 등급 환산표, JLPT→TOEIC 환산, 코스명→점수 등 재계산 로직은 이번 범위 밖 (§3) |
| approve/reject/delete 반환 타입 | 요청 스펙대로 `Boolean` 그대로 반환 (컨벤션상 `XxxMutationPayload` 권장이지만 이번엔 예외로 스펙 우선) |
| `fileId` 검증 수준 | 최소 구현이 아니라 제대로 구현 — `FilePersistencePort`를 신규 추가해 파일 존재 여부 + 소유자(`userId`) 일치를 검증 |
| `deleteScore` 허용 범위 | 상태 무관 전체 허용 (`APPROVED`도 삭제 가능) |
| 반려 후 재제출 처리 | 새 row를 만들지 않고, **동일 사용자+카테고리의 기존 `REJECTED` row를 찾아 값을 갈아끼우고 상태를 `PENDING`으로 되돌림** (§8.4) |
| add* 뮤테이션 호출 권한 | `STUDENT`만 호출 가능 (본인 명의로만 신청, `MemberUtil.getCurrentUserId()` 사용 → `My` 네이밍) |
| approve/reject/delete 호출 권한 | `isTeacherOrAbove()`(`TEACHER`/`HOMEROOM_TEACHER`/`ROOT`) — 기존 Query 서비스들과 동일 기준 재사용, 담임 학급 범위 제한은 미구현(기존 코드에도 없음) |

## 3. 알려진 이슈 / 이번 범위에서 다루지 않는 것

사용자가 제공한 실제 "GSM 역량인증제 안내 자료"를 보면 카테고리별 점수 산정 규칙이 상당히 복잡하다
(자격증 개당 2점, TOPCIT 100점당 1점 반올림, JLPT→TOEIC 환산, 독서마라톤 코스별 고정점수+단계 가산,
교과성적 등급→점수 환산표, 토익사관학교 참여시 TOEIC/JLPT 카테고리에 +1 가산점 등). 이런 "제출값을
실제 인정 점수로 재계산"하는 로직은 이번 4개 add 뮤테이션의 책임이 아니라 **이미 구현되어 있는
`ScoreCalculator`(집계 엔진)의 몫**으로 명확히 분리한다. 이번 작업은 학생이 제출한 원시값을 정확한
필드(`scoreValue` 또는 `activityName`)에 정확히 저장하는 것까지만 담당한다.

후속 작업으로 남겨두는 항목:

- `ScoreCalculator.recognizedScore`가 미누적(`isAccumulated=false`) `SCORE_BASED` 카테고리에서
  **최신값**을 인정하는 로직 — 실제 정책은 TOPCIT/TOEIC/JLPT에 대해 "여러 번 제출 가능, **최고점수** 인정"
- `TOEIC_ACADEMY` 참여가 `TOEIC`/`JLPT` 카테고리 점수에 +1 가산점을 주는 카테고리 간 가산 로직 (현재
  구조는 카테고리별 독립 합산만 지원)
- 독서마라톤 코스명(거북이/악어/토끼/...)→점수, 교과성적 등급→점수, JLPT→TOEIC 환산, 뉴로우스쿨
  회고온도÷20 등 카테고리별 전용 환산 규칙

## 4. 선행 작업 — 포트/엔티티/스키마 갭 해소

### 4.1 신규 아웃바운드 포트 — `category` 도메인

`category` 도메인에는 조회 포트가 전혀 없다. 신규 추가:

```
domain/category/port/out/CategoryPersistencePort.kt   — findByCategoryType(categoryType): Category?
domain/category/adapter/out/persistence/CategoryPersistenceAdapter.kt
```

### 4.2 신규 아웃바운드 포트 — `file` 도메인

```
domain/file/port/out/FilePersistencePort.kt            — findById(fileId): File?
domain/file/adapter/out/persistence/FilePersistenceAdapter.kt
```

`FileJpaEntity.score`(FK)가 `nullable=true`이며 `@PrePersist`로 "score 또는 evidence 중 하나는
반드시 존재"를 검증한다. `addScoreWithFile`이 기존 파일(`fileId`)을 새로 만든 `Score`에 연결할 때는
**INSERT가 아니라 UPDATE**이므로 `@PrePersist` 제약과 무관하게 `score_id` 컬럼만 갈아끼우면 된다.

### 4.3 `ScorePersistencePort` — 쓰기 메서드 추가

현재 조회만 있고 저장/삭제가 없다. 추가:

```kotlin
fun save(score: Score): Score
fun deleteById(scoreId: Long)
```

`ScorePersistenceAdapter`는 현재 `JPAQueryFactory`만 주입받아 QueryDSL로만 조회한다. 저장/삭제를 위해
`ScoreJpaRepository : JpaRepository<ScoreJpaEntity, Long>` 신규 추가 후 어댑터에 함께 주입.

### 4.4 `ScoreJpaEntity.rejectionReason` 컬럼 길이

요청 스펙은 "거절 사유 최대 500자"이나 현재 컬럼은 `length = 255`. `255 → 500`으로 컬럼 정의 변경 +
실제 DB `ALTER TABLE score_tb MODIFY rejection_reason VARCHAR(500)` 수동 반영 필요 (마이그레이션 도구 없음, `ddl-auto: validate`).

### 4.5 `CategoryType.kt` / `ScoreCalculationType.kt` 문서 갱신

- `CategoryType` enum에 `NCS`("직업기초능력평가"), `NEWRROW_SCHOOL`("뉴로우스쿨참여") 추가
- KDoc 표의 `VOLUNTEER`(FILE→UNREQUIRED), `TOEIC_ACADEMY`(EVIDENCE→UNREQUIRED), `READ_A_THON`(EVIDENCE→FILE) 갱신
- `NCS`는 숫자(등급 평균)만 입력받고 파일이 필요 없어 `SCORE_BASED` + `UNREQUIRED`로 기재 (`addScoreWithValue` 버킷)
- `NEWRROW_SCHOOL`(회고온도÷20)은 자료상 수치 환산 후 최댓값 캡을 씌우는 형태로 보여 `SCORE_BASED` + `FILE`로
  문서에 기재 (`addScoreWithFile` 버킷 유지)
- 실제 `category_tb` row 생성/`evidenceType` 확정은 범위 밖이므로 운영 시 재확인 필요

### 4.6 기존 미사용 스키마 정리

`score.graphqls`에 이미 선언되어 있는 `ScoreInput { value, fileId, evidenceId }`,
`UpdateScoreStatusInput { scoreStatus }`는 이번에 구현하는 6개 뮤테이션 중 어느 것과도 시그니처가
일치하지 않는 죽은 선언이다(참조하는 코드 없음). 이번 작업에서 **삭제**한다.

## 5. GraphQL 스키마 변경 (`score.graphqls`)

```graphql
input ScoreWithValueAndFileInput {
    value: String
    fileId: ID!
}

input ScoreWithValueInput {
    value: String
}

type Mutation {
    addScoreWithFile(categoryType: CategoryType!, input: ScoreWithValueAndFileInput!): Score!
    addScoreWithEvidence(categoryType: CategoryType!, input: ScoreWithValueInput!): Score!
    addScoreWithValue(categoryType: CategoryType!, input: ScoreWithValueInput!): Score!
    addScoreOnly(categoryType: CategoryType!): Score!
    approveScore(scoreId: ID!): Boolean!
    rejectScore(scoreId: ID!, input: RejectScoreInput!): Boolean!
    deleteScore(scoreId: ID!): Boolean!
}
```

- `RejectScoreInput`은 기존 선언을 그대로 재사용 (`rejectionReason: String!`, §4.4로 컬럼 길이만 조정)
- `CategoryType`에 `NCS`, `NEWRROW_SCHOOL` 추가 (`category.graphqls`)
- 응답의 `categoryNames { englishName koreanName }`은 기존 `Score.category.categoryEnglishName` /
  `categoryKoreanName` 필드로 클라이언트가 직접 조회 (별도 타입 신설 불필요)
- 이 `type Mutation`이 **프로젝트 전체 최초의 Mutation 선언**이다. `project-participation-plan.md`의
  `submitProjectParticipation`/`scoresByDgProjectId`는 이 파일에 `extend type Mutation`/`extend type Query`로
  추가되며, `project.graphqls`도 `extend`를 사용한다

## 6. 아키텍처 (Hexagonal) — 계층별 작업

```
domain/category/port/out/CategoryPersistencePort.kt                (신규)
domain/category/adapter/out/persistence/CategoryPersistenceAdapter.kt (신규)
domain/file/port/out/FilePersistencePort.kt                          (신규 — findById, findByScoreId, linkToScore, unlinkFromScore)
domain/file/adapter/out/persistence/FilePersistenceAdapter.kt        (신규)

domain/score/port/out/ScorePersistencePort.kt          (save/deleteById 추가)
domain/score/adapter/out/persistence/ScoreJpaRepository.kt           (신규)
domain/score/adapter/out/persistence/ScorePersistenceAdapter.kt      (save/deleteById 구현 추가)

domain/score/port/in/
  AppendMyScoreWithFileUseCase.kt
  AppendMyScoreWithEvidenceUseCase.kt
  AppendMyScoreWithValueUseCase.kt
  AppendMyScoreOnlyUseCase.kt
  ApproveScoreUseCase.kt
  RejectScoreUseCase.kt
  RemoveScoreUseCase.kt

domain/score/service/
  AppendMyScoreWithFileService.kt
  AppendMyScoreWithEvidenceService.kt
  AppendMyScoreWithValueService.kt
  AppendMyScoreOnlyService.kt
  ApproveScoreService.kt
  RejectScoreService.kt
  RemoveScoreService.kt

domain/score/adapter/web/ScoreWebAdapter.kt   (@MutationMapping 7종 추가)
```

- 4개 범용 add 서비스는 카테고리 검증(§8.1) + value 매핑(§8.2) + 재제출 판단(§8.4)을 공유하는 로직이 많다.
  중복을 피하기 위해 `AppendScoreSupport`류의 내부 헬퍼(또는 `ScoreCalculator`처럼 순수 도메인 오브젝트)로
  공통화하는 걸 권장하되, 서비스 클래스/유스케이스 자체는 컨벤션에 따라 4개로 분리 유지
- 컨트롤러 파라미터명은 컨벤션에 따라 `input`으로 고정 (`categoryType`은 input이 아닌 별도 스칼라 인자이므로 그대로 유지)
- `submitProjectParticipation`/`scoresByDgProjectId`(신규 UseCase/Service)는 `project-participation-plan.md`에서
  별도로 다루며, `domain/score`에 함께 추가되지만 이 문서의 4개+3개 목록에는 포함하지 않음

## 7. 접근 제어 / 예외

새로운 `ErrorCode` 추가:

| 코드 | HTTP | 설명 | 사용처 |
| --- | --- | --- | --- |
| `CATEGORY_NOT_FOUND` | 404 | 카테고리를 찾을 수 없습니다 | `categoryType`에 대응하는 `Category` row 없음 |
| `INVALID_CATEGORY_TYPE` | 400 | 해당 요청에 사용할 수 없는 카테고리입니다 | §8.1 검증 실패 (예: `AWARD`를 `addScoreWithFile`로 호출) |
| `INVALID_SCORE_VALUE` | 400 | 점수 값이 올바르지 않습니다 | `SCORE_BASED` 카테고리에 숫자로 파싱 불가한 `value` 전달 |
| `FILE_NOT_FOUND` | 404 | 파일을 찾을 수 없습니다 | `fileId`에 대응하는 파일 없음 |
| `FORBIDDEN` | 403 | (기존 재사용) | 본인 소유가 아닌 `fileId`, `STUDENT` 아닌 사용자의 add* 호출, `TEACHER` 미만의 approve/reject/delete 호출 |
| `SCORE_NOT_FOUND` | 404 | (기존 재사용) | approve/reject/delete 대상 `scoreId` 없음 |

(`PROJECT_PARTICIPATION` 관련 에러코드는 `project-participation-plan.md`에서 별도 관리)

## 8. 비즈니스 로직 상세

### 8.1 카테고리-뮤테이션 매칭 규칙

`categoryType`으로 조회한 `Category`의 `evidenceType`/`calculationType`을 기준으로 판단 (하드코딩 목록 없음):

| 뮤테이션 | 조건 |
| --- | --- |
| `addScoreWithFile` | `evidenceType == FILE` |
| `addScoreWithEvidence` | `evidenceType == EVIDENCE` |
| `addScoreWithValue` | `evidenceType == UNREQUIRED && calculationType == SCORE_BASED` |
| `addScoreOnly` | `evidenceType == UNREQUIRED && calculationType == COUNT_BASED` |

조건 불일치 시 `GsmcException(ErrorCode.INVALID_CATEGORY_TYPE)`.

§4.5의 재정의(`VOLUNTEER`→UNREQUIRED/SCORE_BASED, `TOEIC_ACADEMY`→UNREQUIRED/COUNT_BASED,
`READ_A_THON`→FILE/SCORE_BASED, `NCS`→UNREQUIRED/SCORE_BASED) 이후에는 이 표가 요청 스펙의 카테고리
그룹핑과 정확히 일치한다.

예외: `PROJECT_PARTICIPATION`은 `evidenceType == EVIDENCE`라 위 표대로면 `addScoreWithEvidence`
대상이지만, DataGSM 연동이 필요해 `project-participation-plan.md`의 전용 뮤테이션(`submitProjectParticipation`)
으로만 신청하도록 `addScoreWithEvidence`의 허용 조건에 `categoryType != PROJECT_PARTICIPATION`을 추가로
검사한다 (이 한 가지만 카테고리 화이트리스트 예외로 하드코딩).

### 8.2 `value` → `scoreValue` / `activityName` 매핑

| 뮤테이션 | `value` 처리 |
| --- | --- |
| `addScoreWithFile` | `calculationType == SCORE_BASED`면 정수 파싱 후 `scoreValue`, `COUNT_BASED`면 원문 그대로 `activityName` |
| `addScoreWithEvidence` | 항상 `activityName` (이 버킷은 항상 `COUNT_BASED`) |
| `addScoreWithValue` | 항상 정수 파싱 후 `scoreValue` (이 버킷은 항상 `SCORE_BASED`), 파싱 실패 시 `INVALID_SCORE_VALUE` |
| `addScoreOnly` | `value` 자체가 없음 — `scoreValue = null`, `activityName = null` |

### 8.3 신규 생성 `Score`의 초기 상태

4개 add 뮤테이션 모두 `ScoreStatus.PENDING`으로 생성 (교사 검토 대기 — `INCOMPLETE`에 도달하는 별도
플로우는 이번 범위에 없음).

### 8.4 재제출 처리

add* 뮤테이션 호출 시, **현재 로그인 사용자 + 해당 `categoryType`**으로 기존 `Score` 중
`scoreStatus == REJECTED`인 row가 있으면 그 row를 재사용한다 — 새 값(`activityName`/`scoreValue`/`file`
연결)으로 갈아끼우고 `rejectionReason = null`, `scoreStatus = PENDING`으로 되돌려 저장한다. 없으면 새
`Score` row를 INSERT한다. (기존 `PENDING`/`APPROVED` row가 있어도 별도 중복 검사 없이 새 row를 추가로
생성 — 인증제 자료상 "여러 번 제출 가능" 정책과 일치)

### 8.5 `approveScore` / `rejectScore` / `deleteScore`

- 상태 전이 유효성(예: 이미 `APPROVED`인 건을 다시 승인 등)은 검증하지 않고 무조건 적용 — 스펙의 에러
  테이블에 `NOT_FOUND` 외 다른 코드가 없음
- `rejectScore`는 `scoreStatus = REJECTED`, `rejectionReason` 저장
- `deleteScore`는 상태 무관 하드 삭제 (§2)

## 9. 작업 순서 (체크리스트)

1. [ ] `CategoryPersistencePort`/`Adapter`, `FilePersistencePort`/`Adapter` 신규 구현 (`EvidencePersistencePort`는
   이 범용 뮤테이션들에서 실제로 쓰이지 않아 이번 범위에서 제외 — `project-participation-plan.md`에서 신규 구현)
2. [ ] `ScoreJpaRepository` 신규 추가, `ScorePersistencePort`에 `save`/`deleteById` 추가 및 어댑터 구현
3. [ ] `ScoreJpaEntity.rejectionReason` 컬럼 길이 500으로 변경 + DB 수동 반영
4. [ ] `CategoryType`에 `NCS`, `NEWRROW_SCHOOL` 추가, KDoc 갱신 (NCS는 UNREQUIRED/SCORE_BASED로 기재)
5. [ ] `category_tb`의 `VOLUNTEER`/`TOEIC_ACADEMY`/`READ_A_THON` `evidenceType` 값 갱신 (DB 수동 반영) — 로컬/개발 환경부터
6. [ ] `score.graphqls`에 `ScoreWithValueAndFileInput`, `ScoreWithValueInput`, `type Mutation` 추가, 미사용 `ScoreInput`/`UpdateScoreStatusInput` 삭제
7. [ ] `ErrorCode`에 `CATEGORY_NOT_FOUND`, `INVALID_CATEGORY_TYPE`, `INVALID_SCORE_VALUE`, `FILE_NOT_FOUND` 추가
8. [ ] `port/in` UseCase 7종 정의
9. [ ] `service` 구현체 7종 (§8 로직 포함, 4개 범용 add는 공통 검증 로직 헬퍼로 공유)
10. [ ] `ScoreWebAdapter`에 `@MutationMapping` 7종 추가
11. [ ] KtLint 포맷 적용
12. [ ] GraphiQL로 4개 카테고리 그룹(FILE/EVIDENCE/VALUE/ONLY) × 정상/에러 케이스, 재제출(REJECTED→재사용), 승인/거절/삭제 권한 케이스 수동 검증

프로젝트 참여(`PROJECT_PARTICIPATION`) 관련 작업은 [`project-participation-plan.md`](./project-participation-plan.md)의 체크리스트를 따른다.
