# Score 도메인 조회 GraphQL Query API 구현 계획

- 관련 이슈: [#35 \[score\] 점수 조회 GraphQL Query API 구현](https://github.com/team-incube/GSMC-server-V4/issues/35)
- 작성일: 2026-07-02

## 1. 개요

학생 본인의 점수 현황(목록/단건/카테고리별/총점/백분위)과, 교사가 특정 학생의 점수를 조회할 수 있는
GraphQL Query 8종을 구현한다. `score` 도메인에는 현재 어댑터/포트/서비스 계층이 전혀 없고
(`ScoreStatus.kt`, `ScoreJpaEntity.kt`만 존재), GraphQL 스키마도 Object Type 정의(`Score`, `Category`,
`Evidence`, `File`, `User`)만 되어 있고 Query/Resolver는 프로젝트 전체에 하나도 구현되어 있지 않다.
이번 작업이 이 프로젝트의 **첫 GraphQL Query 리졸버 구현**이다.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| 엔티티 보강 범위 | 이번 작업 범위에 포함. `ScoreJpaEntity`, `CategoryJpaEntity`에 컬럼 추가 |
| `scoresByCategory` / `totalScore(memberId)` 접근 제어 | `TEACHER` 이상(`TEACHER`, `HOMEROOM_TEACHER`, `ROOT`)만 허용 |
| `score(scoreId)` 접근 제어 | 본인 소유이거나 `TEACHER` 이상만 허용, 그 외 `FORBIDDEN` |
| Evidence/File 필드명 | 기존 스키마 필드명 유지 (`evidenceTitle`, `fileOriginalName`, `fileUri` 등). 요청 스펙 문서의 `title`/`originalName`/`uri`는 축약 표기로 간주 |
| 총점/인정점수 계산 | `weight` 곱셈 + `categoryMaximumValue` 캡을 모두 적용. `isAccumulated=true`면 승인 점수 합산, `false`면 최신값 대체 |
| 백분위 모수 | 같은 반(`myPercentInClass`) / 같은 학년(`myPercentInGrade`)의 `UserRole=STUDENT` 전체 |
| 백분위 동점자 처리 | 표준 경기 순위(공동순위 + 다음 순위 건너뛰기, 1-2-2-4) |
| 백분위 반올림 | 정수 반올림 |
| 교사가 백분위 API 호출 시 | `GsmcException(ErrorCode.FORBIDDEN)` |
| `myScores` 정렬/페이징 | 서버 정렬 기준은 중요하지 않음(프론트에서 재정렬). 페이징 없음, `scoreId` 오름차순 기본값 |

## 3. 선행 작업 — 엔티티/스키마 갭 해소

### 3.1 `ScoreJpaEntity` 컬럼 추가

현재 컬럼: `scoreId`, `user`, `category`, `evidence`, `scoreStatus`, `activityName`

추가 필요:
- `scoreValue: Int?` — 인정 점수 값 (`score_value` 컬럼, nullable — 심사 전에는 값 없음)
- `createdAt: LocalDateTime` — `@CreationTimestamp` (`created_at`, not null)
- `updatedAt: LocalDateTime` — `@UpdateTimestamp` (`updated_at`, not null)
- `rejectionReason: String?` — 반려 사유 (`rejection_reason`, nullable, length 255)

### 3.2 `CategoryJpaEntity` 컬럼 추가

이슈 #33에서 이미 보류된 항목. 현재 `categoryType`, `calculationType` 컬럼이 엔티티에 없어
`myScores(categoryType: ...)` 필터링, `myScoresByCategory`의 `categoryType` 그룹핑,
총점 계산 시 `calculationType` 분기가 불가능하다.

추가 필요:
- `categoryType: CategoryType` — `@Enumerated(STRING)` (`category_type`, not null)
- `calculationType: ScoreCalculationType` — `@Enumerated(STRING)` (`calculation_type`, not null)

### 3.3 DB 반영

`ddl-auto: validate`이므로 Hibernate가 스키마를 자동 생성하지 않는다. 마이그레이션 도구(Flyway/Liquibase)도
프로젝트에 없으므로, 실제 MySQL 스키마에 `ALTER TABLE score_tb ADD COLUMN ...`,
`ALTER TABLE category_tb ADD COLUMN ...`을 **수동으로 반영**해야 엔티티 검증이 통과한다.
이 작업은 PR 코드 변경과 별개로 DB 작업으로 진행 — 로컬/개발 DB부터 순서대로 적용.

## 4. GraphQL 스키마 변경 (`score.graphqls`)

```graphql
type Score {
    scoreId: ID!
    userId: ID!
    category: Category!
    evidence: Evidence
    scoreStatus: ScoreStatus!
    activityName: String
    scoreValue: Int
    rejectionReason: String
    updatedAt: DateTime!
}

type ScoreCategoryGroup {
    categoryType: CategoryType!
    categoryEnglishName: String!
    categoryKoreanName: String!
    recognizedScore: Int!
    scores: [Score!]!
}

type TotalScore {
    totalScore: Int!
}

type Percentile {
    topPercentile: Int!
    bottomPercentile: Int!
}

extend type Query {
    myScores(categoryType: CategoryType, status: ScoreStatus): [Score!]!
    score(scoreId: ID!): Score!
    myScoresByCategory(status: ScoreStatus): [ScoreCategoryGroup!]!
    scoresByCategory(memberId: ID!, status: ScoreStatus): [ScoreCategoryGroup!]!
    myTotalScore(includeApprovedOnly: Boolean = true): TotalScore!
    totalScore(memberId: ID!, includeApprovedOnly: Boolean = true): TotalScore!
    myPercentInClass(includeApprovedOnly: Boolean = true): Percentile!
    myPercentInGrade(includeApprovedOnly: Boolean = true): Percentile!
}
```

- `categoryNames { englishName koreanName }` 요청 스펙은 기존 `Category` 타입의
  `categoryEnglishName`/`categoryKoreanName` 필드로 대체한다. `ScoreCategoryGroup`은 category 하나를
  그대로 참조하지 않고 그룹 요약 필드만 노출한다 (요청 스펙에 `categoryNames`만 필요하고 weight 등은 불필요).
- `evidence { evidenceId title }`, `file { id originalName uri }`는 기존 `Evidence`/`File` 타입을
  그대로 재사용 — GraphQL 클라이언트는 필요한 필드만 선택 조회하면 된다.

## 5. 아키텍처 (Hexagonal) — 계층별 작업

```
adapter/in/graphql/ScoreQueryWebAdapter.kt
port/in/
  FetchMyScoresUseCase.kt
  FetchScoreUseCase.kt
  FetchMyScoresByCategoryUseCase.kt
  FetchScoresByCategoryUseCase.kt
  FetchMyTotalScoreUseCase.kt
  FetchTotalScoreUseCase.kt
  FetchMyPercentInClassUseCase.kt
  FetchMyPercentInGradeUseCase.kt
port/out/
  ScorePersistencePort.kt (조회 메서드 추가)
service/
  FetchMyScoresService.kt
  FetchScoreService.kt
  FetchMyScoresByCategoryService.kt   (FetchScoresByCategoryService와 내부 로직 공유)
  FetchScoresByCategoryService.kt
  FetchMyTotalScoreService.kt         (FetchTotalScoreService와 내부 로직 공유)
  FetchTotalScoreService.kt
  FetchMyPercentInClassService.kt
  FetchMyPercentInGradeService.kt
adapter/out/persistence/
  ScorePersistenceAdapter.kt (QueryDSL 조회 구현 추가)
  repository/ScoreJpaRepository.kt (신규)
  repository/ScoreQueryDslRepository.kt (신규, QueryDSL)
```

컨벤션(`convention.md`)에 따라 서비스명은 `Fetch` 접두사 + 본인 대상은 `My` 키워드를 붙인다.
컨트롤러 파라미터명은 `input`으로 고정하는 규칙이 있으나, 이번 API는 전부 Query이며 단일 스칼라/객체
input DTO가 없는 단순 인자 조합이라 `@Argument`를 그대로 받는다 (Query에는 `input` 고정 규칙이
적용되지 않음 — Mutation 컨벤션).

`MemberUtil.getCurrentUserId()`로 로그인 사용자 ID를 얻고, 역할 확인은 `SecurityContextHolder`의
`CustomUserDetails`에서 `UserRole`을 꺼내 판단한다 (현재 `MemberUtil`에는 role 조회 메서드가 없으므로
`getCurrentUserRole()` 또는 `getCurrentUser(): UserJpaEntity` 추가가 필요할 수 있음 — 구현 시 확인).

## 6. 접근 제어 / 예외

새로운 `ErrorCode` 추가 필요:
- `SCORE_NOT_FOUND` (404) — `score(scoreId)` 대상이 없을 때
- `USER_NOT_FOUND`는 기존 재사용 (memberId 대상 사용자 없음)
- `FORBIDDEN`은 기존 재사용 — 아래 케이스에 사용:
  - `score(scoreId)`: 요청자가 소유자가 아니고 `TEACHER` 미만
  - `scoresByCategory`, `totalScore(memberId)`: 요청자가 `TEACHER` 미만
  - `myPercentInClass`, `myPercentInGrade`: 요청자가 `STUDENT`가 아님

## 7. 계산 로직

### 7.1 인정 점수 (`recognizedScore`) / 총점 (`totalScore`)

`ScoreCalculationType.kt`에 이미 문서화된 규칙(COUNT_BASED=건수, SCORE_BASED=scoreValue 합산)을
기준으로 삼는다. 카테고리별로:

1. 대상 점수 집합 필터링 — `includeApprovedOnly=true`(기본값)면 `APPROVED`만, `false`면 전체 상태 포함.
   `myScoresByCategory`/`scoresByCategory`의 `status` 파라미터는 이 필터와 별개로 응답에 포함할
   `scores` 리스트 자체를 필터링하는 용도.
2. 원점수(raw) 계산은 `calculationType`과 `isAccumulated`를 함께 사용:
   - `COUNT_BASED`: `isAccumulated=true`면 필터링된 항목 개수(`list.size`), `false`면 존재 여부(0 또는 1).
   - `SCORE_BASED`: `isAccumulated=true`면 필터링된 항목의 `scoreValue` 합산, `false`면 가장 최근
     (`updatedAt` 최신) 1건의 `scoreValue`.
3. `min(raw, categoryMaximumValue)` 로 캡.
4. `step3 * category.weight` = 해당 카테고리 `recognizedScore`.

`totalScore` = 전체 카테고리 `recognizedScore` 합.

### 7.2 백분위

1. 모수(`totalCount`): `myPercentInClass` → 같은 `userGrade` + `userClassNumber`의 `UserRole=STUDENT`
   전체. `myPercentInGrade` → 같은 `userGrade`의 `UserRole=STUDENT` 전체. 학교 전체 학생 수가 아니라
   해당 스코프(반/학년) 내 인원수로만 나눈다. 반/학년에 본인 혼자만 있는 경우는 구조상 없으므로
   `totalCount=1` 예외 처리는 하지 않는다.
2. 각 학생의 `totalScore`(위 7.1 로직, 동일한 `includeApprovedOnly` 기준 적용)를 계산.
3. `rank` = 본인보다 `totalScore`가 높은 학생 수 + 1 (표준 경기 순위, 동점자는 같은 rank).
4. `topPercentile = round((totalCount - rank + 1) / totalCount * 100)` — 1등일수록 100에 가까움
5. `bottomPercentile = round(rank / totalCount * 100)` — 1등일수록 낮은 값 (한국 수능 백분위와 동일한 방향)

예시 (반 20명, `includeApprovedOnly` 동일 기준):

| 등수(rank) | topPercentile | bottomPercentile |
| --- | --- | --- |
| 1등 | round(20/20*100) = 100 | round(1/20*100) = 5 |
| 3등 | round(18/20*100) = 90 | round(3/20*100) = 15 |
| 20등(꼴찌) | round(1/20*100) = 5 | round(20/20*100) = 100 |

## 8. 작업 순서 (체크리스트)

1. [ ] `ScoreJpaEntity`, `CategoryJpaEntity` 컬럼 추가 + 로컬/개발 DB 스키마 수동 반영
2. [ ] `score.graphqls` 스키마 확장 (섹션 4)
3. [ ] `ErrorCode.SCORE_NOT_FOUND` 추가
4. [ ] `MemberUtil`에 현재 사용자 역할 조회 기능 필요 시 추가
5. [ ] `port/out/ScorePersistencePort` + QueryDSL 어댑터 구현 (목록/단건/카테고리 그룹/학급·학년 전체 조회)
6. [ ] `port/in` UseCase 8종 정의
7. [ ] `service` 구현체 8종 (권한 검증 + 계산 로직 포함)
8. [ ] `adapter/in/graphql` Query 리졸버 구현 (`@QueryMapping`)
9. [ ] KtLint 포맷 적용
10. [ ] 각 API에 대해 GraphiQL 또는 통합 테스트로 권한/필터/계산 결과 수동 검증
