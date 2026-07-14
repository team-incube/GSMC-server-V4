# 카테고리 조회/검색 API 구현 계획

- 작성일: 2026-07-14
- 관련 문서: 없음

## 1. 개요

`category` 도메인은 현재 `domain` / `port/out` / `adapter/out`(영속성) 계층까지만 구현되어 있고,
`port/in`(UseCase) / `service` / `adapter/web`(GraphQL 리졸버)은 없다. 즉 카테고리를 직접 조회하는
GraphQL Query가 아직 하나도 없으며, `score` 도메인이 `CategoryPersistencePort.findByCategoryType`을
내부적으로 소비하는 형태로만 쓰이고 있다.

`src/main/resources/graphql/category.graphqls`에는 `type Category`가 이미 정의되어 있고
(`categoryEnglishName`, `categoryKoreanName`, `categoryMaximumValue` 등) `score.graphqls`의 `Score` 타입이
이를 중첩 필드로 이미 참조 중이다. 이번에 추가할 티켓 스펙의 응답 필드명(`englishName`, `koreanName`,
`maxRecordCount`)은 이 기존 필드명과 다르다 — 기존 `Category` 타입을 건드리면 `Score` 조회 쪽에 영향이
가므로, 인터뷰 결과 **신규 `CategoryPayload` GraphQL 타입을 별도로 만들어** 티켓 스펙 필드명을 그대로
쓰기로 했다.

카테고리 데이터(`category_tb`)는 `CategoryType` enum 기준 13개 행뿐인 사실상 정적인 참조 테이블이다.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| 응답 GraphQL 타입 | 기존 `Category` 타입을 건드리지 않고 신규 `CategoryPayload` 타입 생성. 필드명은 티켓 스펙 그대로(`englishName`, `koreanName`, `weight`, `maxRecordCount`, `evidenceType`, `calculationType`) |
| 식별자 포함 여부 | 티켓 스펙 응답에는 없지만 `categoryType`을 추가로 포함한다 — 프론트엔드가 이 조회 결과로 카테고리를 식별해 이후 `addScoreWithFile` 등 Mutation을 호출해야 하기 때문 |
| `searchCategories` 매칭 규칙 | `englishName` 또는 `koreanName` 중 하나라도 keyword를 포함하면 매칭(대소문자 무시, 부분일치) |
| 검색 구현 방식 | QueryDSL(`JPAQueryFactory`)로 DB 레벨에서 필터링. `score` 도메인의 `ScorePersistenceAdapter`가 이미 같은 방식(Q타입 + `queryFactory.selectFrom(...).where(...)`)을 쓰고 있어 일관성을 맞춘다. Spring Data 파생 쿼리 메서드(`findByCategoryEnglishNameContainingIgnoreCaseOrCategoryKoreanNameContainingIgnoreCaseOrderByCategoryIdAsc` 등)는 이름이 지나치게 길어지고 프로젝트 전반의 QueryDSL 사용 관례와도 어긋나 채택하지 않음. 메모리 필터링도 채택하지 않음 |
| `categories` 정렬 기준 | `categoryId` 오름차순 |
| `searchCategories`에 keyword 미전달/빈 문자열 | 필터링 없이 전체 카테고리 반환 (`categories`와 동일한 결과) |
| 인증/권한 수준 | 로그인한 사용자라면 역할(학생/교사/관리자) 무관 허용. 별도 권한 분기 로직 불필요, `/graphql` 엔드포인트의 기본 JWT 인증만 통과하면 됨 |
| 서비스 구조 | Fetch/Search를 완전히 별도의 `UseCase`/`Service`로 분리 (`FetchCategoriesUseCase`/`Service`, `SearchCategoriesUseCase`/`Service`) — convention.md의 Fetch/Search 키워드 규칙을 따름 |

## 3. 데이터 모델

변경 없음. 기존 `category_tb` / `CategoryJpaEntity` / `Category` 도메인 모델을 그대로 사용하고 신규 컬럼은
추가하지 않는다.

`maxRecordCount`는 기존 `categoryMaximumValue`를 그대로 매핑한다. 이 필드는 `COUNT_BASED`(예:
자격증 개수)뿐 아니라 `SCORE_BASED`(예: TOPCIT 최대 점수) 카테고리에도 공통으로 쓰이는 값이라 "record
count"라는 이름이 후자의 의미와는 다소 어긋나지만, 실제 값은 하나이고 이번 요청은 티켓 스펙의 필드명을
그대로 쓰기로 했으므로 이름만 그대로 노출한다(§8 참고).

## 4. 신규/변경 파일

```
domain/category/port/out/CategoryPersistencePort.kt          — 변경: findAll(), searchByKeyword(keyword) 추가
domain/category/adapter/out/persistence/CategoryPersistenceAdapter.kt
                                                               — 변경: JPAQueryFactory 주입 + QueryDSL로 신규 포트 메서드 구현
                                                                 (CategoryJpaRepository는 기존 findByCategoryType만 그대로 유지, 변경 없음)

domain/category/port/in/FetchCategoriesUseCase.kt             — 신규
domain/category/port/in/SearchCategoriesUseCase.kt            — 신규
domain/category/service/FetchCategoriesService.kt             — 신규
domain/category/service/SearchCategoriesService.kt            — 신규

domain/category/adapter/web/CategoryPayload.kt                — 신규: 응답 DTO + Category.toPayload() 확장함수
domain/category/adapter/web/CategoryWebAdapter.kt              — 신규: @QueryMapping categories/searchCategories

src/main/resources/graphql/category.graphqls                  — 변경: CategoryPayload 타입 + extend type Query 추가
```

`ProjectWebAdapter`/`ScoreWebAdapter`와 동일하게 GraphQL 인바운드 어댑터 패키지는 `adapter/web`을 쓴다
(아키텍처 문서의 `adapter/in`은 이 코드베이스에서 실제로는 `adapter/web`으로 명명되어 있음). Mutation
Input DTO가 `domain/score/adapter/web/ScoreWithValueInput.kt`처럼 어댑터 패키지에 flat하게 위치하는
선례를 따라 `CategoryPayload`도 같은 패키지에 둔다.

## 5. GraphQL 스키마

**`category.graphqls` 추가분** (기존 `type Category`/enum 선언은 그대로 유지)

```graphql
type CategoryPayload {
    categoryType: CategoryType!
    englishName: String!
    koreanName: String!
    weight: Int!
    maxRecordCount: Int!
    evidenceType: EvidenceType!
    calculationType: ScoreCalculationType!
}

extend type Query {
    categories: [CategoryPayload!]!
    searchCategories(keyword: String): [CategoryPayload!]!
}
```

## 6. 신규 `ErrorCode`

없음. 두 Query 모두 결과가 없으면 예외 없이 빈 리스트를 반환한다(카테고리 부재는 예외적 상황이 아님).

## 7. 처리 흐름

1. **`categories`**: (인증은 Spring Security 필터에서 전역 처리) →
   `CategoryPersistencePort.findAll()` (categoryId 오름차순 정렬) → 각 `Category`를
   `CategoryPayload`로 매핑 → 반환
2. **`searchCategories(keyword)`**:
   - `keyword`가 `null`이거나 blank → `FetchCategoriesUseCase`와 동일하게 `findAll()` 결과 전체 반환
   - 그 외 → `CategoryPersistencePort.searchByKeyword(keyword)` (영문명/한글명 대소문자 무시 부분일치,
     categoryId 오름차순) → `CategoryPayload` 매핑 → 반환

### `CategoryPersistenceAdapter` QueryDSL 구현(예시)

`ScorePersistenceAdapter`와 동일하게 `JPAQueryFactory`를 생성자 주입받아 Q타입(`QCategoryJpaEntity.categoryJpaEntity`)으로 조회한다.

```kotlin
override fun findAll(): List<Category> =
    queryFactory
        .selectFrom(categoryJpaEntity)
        .orderBy(categoryJpaEntity.categoryId.asc())
        .fetch()
        .map { it.toDomain() }

override fun searchByKeyword(keyword: String): List<Category> =
    queryFactory
        .selectFrom(categoryJpaEntity)
        .where(
            categoryJpaEntity.categoryEnglishName.containsIgnoreCase(keyword)
                .or(categoryJpaEntity.categoryKoreanName.containsIgnoreCase(keyword)),
        ).orderBy(categoryJpaEntity.categoryId.asc())
        .fetch()
        .map { it.toDomain() }
```

## 8. 열린 이슈 (의도적으로 범위 밖으로 둔 것)

- `maxRecordCount`라는 필드명이 `SCORE_BASED` 카테고리(TOPCIT/TOEIC 등 "최대 점수")에는 의미상 어색할 수
  있음 — 프론트엔드와 필드 의미를 다시 확인할 여지가 있으나, 이번 구현은 티켓 스펙의 필드명을 그대로 따름
- `CategoryPayload`는 이 코드베이스에서 "도메인과 필드명이 다른" Payload DTO의 첫 사례다. 지금까지는
  GraphQL 타입 필드명과 도메인 프로퍼티명이 항상 일치해 별도 매핑 DTO 없이 도메인 객체를 그대로
  반환해왔다(`ProjectWebAdapter`가 `DataGsmProject`를 그대로 반환하는 식) — 이후 유사 케이스가 생기면
  이번 `Category.toPayload()` 확장함수 패턴을 참고할 수 있음
- `category_tb`는 사실상 불변 데이터라 캐싱(Redis 등) 여지가 있으나 이번 범위에서는 다루지 않음

## 9. 작업 순서 (체크리스트)

1. [x] `CategoryPersistencePort`에 `findAll()`, `searchByKeyword(keyword: String)` 추가
2. [x] `CategoryPersistenceAdapter`에 `JPAQueryFactory` 주입, QueryDSL로 신규 포트 메서드 2종 구현(`toDomain()` 매핑 재사용)
3. [x] `FetchCategoriesUseCase`/`FetchCategoriesService` 구현
4. [x] `SearchCategoriesUseCase`/`SearchCategoriesService` 구현 (keyword blank 분기 포함)
5. [x] `CategoryPayload` data class + `Category.toPayload()` 확장함수 작성
6. [x] `CategoryWebAdapter` 작성 (`@QueryMapping categories`, `@QueryMapping searchCategories`)
7. [x] `category.graphqls`에 `CategoryPayload` 타입 + `extend type Query` 추가
8. [x] KtLint 포맷 적용
9. [ ] GraphiQL로 `categories`/`searchCategories`(keyword 있음/없음/빈 문자열) 수동 검증
