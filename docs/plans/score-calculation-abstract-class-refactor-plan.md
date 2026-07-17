# 점수 계산 로직 추상클래스 리팩터링 계획

- 작성일: 2026-07-17
- 관련 문서: 없음 (관련 커밋: `ca7bb45` 토익사관학교 가산점 로직 반영, `9a9900d` 원점수 인정점수 환산 로직 추가, `ced97dc` category 환산 배율 컬럼 추가)

## 1. 개요

현재 `domain/score` 패키지 루트에는 순수 계산 로직을 담은 `object` 싱글턴 두 개가 있다.

- `ScoreCalculator.kt` — **집계 시점** 로직. `List<Score>`를 카테고리 단위로 압축(`rawScoreOf`, `calculationType` 기준), 카테고리를 가로지르는 오케스트레이션(`categoryGroups`/`totalScoreOf`/`percentileOf`)까지 한 파일에 섞여 있다. `TOEIC` 카테고리만 `TOEIC_ACADEMY` 점수를 별도로 찾아 보너스를 더하는 `recognizedToeicScoreWithAcademyBonus`가 있는데, 이 헬퍼가 `categoryGroups()`와 `totalScoreOf()` 양쪽에서 각각 호출되어 로직이 중복돼 있다.
- `ScoreValueConverter.kt` — **제출 시점** 로직. 사용자가 입력한 원점수 문자열을 파싱한 뒤 카테고리별로 환산한다(`TOPCIT`/`TOEIC`/`NEWRROW_SCHOOL`은 나눗셈, `ACADEMIC_GRADE`/`NCS`는 역순 계산, 나머지는 반올림 그대로). `ACADEMIC_GRADE` 전용 학년별 등급 범위 검증(`validateAcademicGradeRange`)도 같은 object에 있다.

둘 다 `when (category.categoryType)` 분기를 쓰는 절차적 스타일이며, 이 코드베이스에는 **`abstract class`가 단 한 곳도 없다** — 기존 추상화 수단은 `interface`(port) + `object`(계산 로직) + 위임형 `@Component` 헬퍼(`AppendScoreSupport`)뿐이다. 이번 리팩터링은 이 코드베이스에 `abstract class` + 서브클래스 다형성이라는 새 패턴을 처음 도입하는 작업이다.

목표: 카테고리 타입별로 갈리는 계산 로직을 추상클래스 + 서브클래스 구조로 바꾸고, TOEIC(집계)과 ACADEMIC_GRADE/NCS(변환)만 실제로 다른 서브클래스를 갖게 한다. **동작은 전혀 바뀌지 않는 순수 리팩터링**이며, 부수 효과로 `recognizedToeicScoreWithAcademyBonus` 중복 호출도 자연히 해소된다.

## 2. 인터뷰로 확정한 의사결정 사항

| 항목 | 결정 |
| --- | --- |
| 리팩터링 범위 | 집계(`ScoreCalculator`)와 변환(`ScoreValueConverter`) 양쪽 모두 카테고리 타입 기반 다형성으로 전환 |
| 서브클래스 선택(디스패치) 방식 | `object` 레지스트리가 `CategoryType`을 받아 인스턴스를 반환하는 `resolve()`를 제공. domain 레이어는 Spring에 의존하지 않으므로 순수 Kotlin `object`로 구현 (구체적인 조회 방식은 아래 참고) |
| 두 계열의 통합 여부 | 별도 계열 유지. 제출 시점(변환)과 집계 시점(계산)은 호출 시점·입출력 모양이 달라 하나로 합치면 인터페이스가 오염됨 |
| `ScoreCalculator` 서브클래스 개수 | `DefaultScoreCalculator` + `ToeicScoreCalculator` 2개만 존재. 실제로 raw 계산이 다른 건 `calculationType`(COUNT_BASED/SCORE_BASED)뿐이고, `categoryType`이 계산을 바꾸는 경우는 TOEIC 보너스 하나뿐이라 나머지 12개 타입에 억지로 서브클래스를 만들지 않음 |
| TOEIC 보너스 노출 방식 | 기본 추상클래스에 `open fun bonusScore(...): Int = 0` hook으로 정의. 호출부가 구체 타입을 다운캐스팅할 필요 없음 |
| hook 파라미터 모양 | 카테고리별로 그룹핑된 전체 점수 `Map<Category, List<Score>>`를 그대로 전달. TOEIC 서브클래스가 이 Map 안에서 `TOEIC_ACADEMY`를 직접 찾아 계산 |
| `validateAcademicGradeRange` 처리 | `convert()`와는 별도 메서드 호출로 유지(서비스가 여전히 `ACADEMIC_GRADE`일 때만 호출). 다만 계산기 계열과의 일관성을 위해 추상클래스의 `open fun validate(...) {}` hook(기본 no-op)으로 통합 — TOEIC 보너스 hook과 같은 패턴 |
| 레지스트리 조회 방식 | `Map` 리터럴이나 exhaustive `when`(모든 `CategoryType`을 나열)이 아니라, 기존 `ScoreValueConverter.convert()`가 쓰던 것과 동일한 **`when(categoryType) { 특수 케이스들 -> ...; else -> default }`**(또는 예외가 하나뿐이면 `if`) 스타일 유지. 처음엔 "새 카테고리 추가 시 등록을 빠뜨리면 즉시 실패해야 한다"는 이유로 exhaustive 나열을 검토했으나, ① 카테고리 추가는 이미 시드 데이터/GraphQL enum 동기화 등 사람이 체크리스트로 챙겨야 하는 수동 단계가 여럿 있어 레지스트리만 컴파일러로 강제하는 게 일관성이 없고, ② "예외 1~2개 + 나머지는 default"인 실제 규칙이 13줄 나열 속에 묻혀 가독성이 떨어진다는 판단으로 기각 |
| 레지스트리 조회 실패 처리 | 별도 예외 처리 없음 — `else -> default`가 있어 `resolve()`는 어떤 `CategoryType`에도 항상 값을 반환하므로 실패할 수 없음 |
| 파일 배치 | `domain/score/calculator/`, `domain/score/converter/` 서브패키지로 분리. 이 프로젝트가 domain 레이어에 서브패키지를 쓰는 첫 사례 |
| 오케스트레이션(`categoryGroups`/`totalScoreOf`/`percentileOf`) 위치 | 이름을 분리해 `ScoreAggregator`(object, `domain/score` 루트)로 이동. "카테고리 하나짜리 계산"과 "여러 카테고리를 가로지르는 집계"가 이름부터 구분되도록 함 |
| 기존 테스트 처리 | `ScoreCalculatorTest.kt`/`ScoreValueConverterTest.kt`를 서브클래스/레지스트리 단위로 분해해 재작성 |

## 3. 패키지/파일 구조

```
domain/score/
├── Score.kt                                    (변경 없음)
├── ScoreAggregator.kt                           신규 — 기존 ScoreCalculator.categoryGroups/totalScoreOf/percentileOf 이관
├── calculator/
│   ├── ScoreCalculator.kt                       신규 — abstract class (recognizedScore 템플릿 메서드 + rawScoreOf/bonusScore hook)
│   ├── DefaultScoreCalculator.kt                신규
│   ├── ToeicScoreCalculator.kt                  신규 — bonusScore 오버라이드
│   └── ScoreCalculatorRegistry.kt               신규 — CategoryType → 인스턴스 명시적 Map
├── converter/
│   ├── ScoreValueConverter.kt                   신규 — abstract class (convert/validate open 메서드)
│   ├── DefaultScoreValueConverter.kt            신규 — 오버라이드 없음(패스스루 상속)
│   ├── DivisorScoreValueConverter.kt            신규 — TOPCIT/TOEIC/NEWRROW_SCHOOL
│   ├── AcademicGradeScoreValueConverter.kt      신규 — ACADEMIC_GRADE/NCS, validate 오버라이드
│   └── ScoreValueConverterRegistry.kt           신규 — CategoryType → 인스턴스 명시적 Map
└── service/ (아래 §5 목록만 import/호출부 수정, 로직 변경 없음)

삭제:
domain/score/ScoreCalculator.kt                  (계산 부분은 calculator/로, 오케스트레이션은 ScoreAggregator.kt로 이관 후 삭제)
domain/score/ScoreValueConverter.kt              (converter/로 이관 후 삭제)
```

## 4. `calculator/` 설계

### `ScoreCalculator.kt` (abstract class)

```kotlin
package team.incube.gsmc.domain.score.calculator

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score

abstract class ScoreCalculator {
    fun recognizedScore(
        scoresByCategory: Map<Category, List<Score>>,
        category: Category,
    ): Int {
        val base = rawScoreOf(scoresByCategory[category].orEmpty(), category)
        val bonus = bonusScore(scoresByCategory, category)
        return minOf(base + bonus, category.categoryMaximumValue) * category.weight
    }

    protected open fun rawScoreOf(
        scoresInCategory: List<Score>,
        category: Category,
    ): Int =
        when (category.calculationType) {
            ScoreCalculationType.COUNT_BASED ->
                if (category.isAccumulated) {
                    scoresInCategory.size
                } else if (scoresInCategory.isNotEmpty()) {
                    1
                } else {
                    0
                }
            ScoreCalculationType.SCORE_BASED ->
                if (category.isAccumulated) {
                    scoresInCategory.sumOf { it.scoreValue ?: 0 }
                } else {
                    scoresInCategory.maxByOrNull { it.updatedAt }?.scoreValue ?: 0
                }
        }

    protected open fun bonusScore(
        scoresByCategory: Map<Category, List<Score>>,
        category: Category,
    ): Int = 0
}
```

`recognizedScore`는 `final`(템플릿 메서드) — 캡 적용과 가중치 곱셈은 모든 카테고리가 공통으로 따라야 하는 규칙이라 서브클래스가 우회할 수 없게 고정한다. `rawScoreOf`/`bonusScore`만 `open`.

### `DefaultScoreCalculator.kt`

```kotlin
package team.incube.gsmc.domain.score.calculator

class DefaultScoreCalculator : ScoreCalculator()
```

### `ToeicScoreCalculator.kt`

```kotlin
package team.incube.gsmc.domain.score.calculator

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Score

class ToeicScoreCalculator : ScoreCalculator() {
    override fun bonusScore(
        scoresByCategory: Map<Category, List<Score>>,
        category: Category,
    ): Int {
        val (academyCategory, academyScores) =
            scoresByCategory.entries.find { it.key.categoryType == CategoryType.TOEIC_ACADEMY }
                ?: return 0
        return rawScoreOf(academyScores, academyCategory)
    }
}
```

`rawScoreOf`를 그대로 재사용해 `TOEIC_ACADEMY`(COUNT_BASED, 비누적) 점수를 계산한다 — 참여 승인 건이 하나라도 있으면 1, 없으면 0. 기존 `recognizedToeicScoreWithAcademyBonus`와 동일한 값.

### `ScoreCalculatorRegistry.kt`

```kotlin
package team.incube.gsmc.domain.score.calculator

import team.incube.gsmc.domain.category.CategoryType

object ScoreCalculatorRegistry {
    private val default = DefaultScoreCalculator()
    private val toeic = ToeicScoreCalculator()

    fun resolve(categoryType: CategoryType): ScoreCalculator =
        if (categoryType == CategoryType.TOEIC || categoryType == CategoryType.JLPT) toeic else default
}
```

`JLPT`는 실제로는 `TOEIC` 행을 공유하고(`CategoryType.kt` 문서 주석), `AppendScoreSupport.findCategoryOrThrow`가 조회 전에 `TOEIC`으로 캐노니컬 매핑하므로 `Category.categoryType`이 `JLPT`로 관측될 일은 실무 경로상 없다. 그럼에도 안전하게 `toeic`으로 매핑해 둔다.

## 5. `converter/` 설계

### `ScoreValueConverter.kt` (abstract class)

```kotlin
package team.incube.gsmc.domain.score.converter

import team.incube.gsmc.domain.category.Category
import kotlin.math.roundToInt

abstract class ScoreValueConverter {
    open fun convert(
        category: Category,
        rawValue: Double,
    ): Int = rawValue.roundToInt()

    open fun validate(
        rawValue: Double,
        studentGrade: Int,
    ) {}
}
```

### `DefaultScoreValueConverter.kt`

```kotlin
package team.incube.gsmc.domain.score.converter

class DefaultScoreValueConverter : ScoreValueConverter()
```

### `DivisorScoreValueConverter.kt`

```kotlin
package team.incube.gsmc.domain.score.converter

import team.incube.gsmc.domain.category.Category
import kotlin.math.roundToInt

class DivisorScoreValueConverter : ScoreValueConverter() {
    override fun convert(
        category: Category,
        rawValue: Double,
    ): Int = (rawValue / category.conversionDivisor).roundToInt()
}
```

### `AcademicGradeScoreValueConverter.kt`

```kotlin
package team.incube.gsmc.domain.score.converter

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import kotlin.math.roundToInt

class AcademicGradeScoreValueConverter : ScoreValueConverter() {
    private val fiveGradeScaleStudentGrades = setOf(1, 2)

    override fun convert(
        category: Category,
        rawValue: Double,
    ): Int = (category.categoryMaximumValue + 1) - rawValue.roundToInt()

    override fun validate(
        rawValue: Double,
        studentGrade: Int,
    ) {
        val maxValidGrade = if (studentGrade in fiveGradeScaleStudentGrades) 5 else 9
        if (rawValue.roundToInt() !in 1..maxValidGrade) {
            throw GsmcException(ErrorCode.INVALID_SCORE_VALUE)
        }
    }
}
```

이름은 `AcademicGradeScoreValueConverter`지만 실제로는 `ACADEMIC_GRADE`와 `NCS` 둘 다 담당한다(둘 다 역순 환산 공식을 공유). `validate()`는 서비스가 `ACADEMIC_GRADE`일 때만 명시적으로 호출하므로 `NCS`에 대해 이 hook이 실행되는 일은 없다 — 기존 동작과 동일.

### `ScoreValueConverterRegistry.kt`

```kotlin
package team.incube.gsmc.domain.score.converter

import team.incube.gsmc.domain.category.CategoryType

object ScoreValueConverterRegistry {
    private val default = DefaultScoreValueConverter()
    private val divisor = DivisorScoreValueConverter()
    private val academicGrade = AcademicGradeScoreValueConverter()

    fun resolve(categoryType: CategoryType): ScoreValueConverter =
        when (categoryType) {
            CategoryType.TOPCIT, CategoryType.TOEIC, CategoryType.JLPT, CategoryType.NEWRROW_SCHOOL -> divisor
            CategoryType.ACADEMIC_GRADE, CategoryType.NCS -> academicGrade
            else -> default
        }
}
```

## 6. `ScoreAggregator.kt` (오케스트레이션, `domain/score` 루트)

```kotlin
package team.incube.gsmc.domain.score

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.calculator.ScoreCalculatorRegistry
import kotlin.math.roundToInt

object ScoreAggregator {
    fun categoryGroups(
        allScores: List<Score>,
        statusFilter: ScoreStatus?,
    ): List<ScoreCategoryGroup> {
        val approvedByCategory = allScores.filter { it.scoreStatus == ScoreStatus.APPROVED }.groupBy { it.category }
        val displayScores = if (statusFilter != null) allScores.filter { it.scoreStatus == statusFilter } else allScores

        val categories = allScores.map { it.category }.distinct()

        return categories
            .filterNot { it.categoryType == CategoryType.TOEIC_ACADEMY }
            .map { category ->
                val recognized = ScoreCalculatorRegistry.resolve(category.categoryType).recognizedScore(approvedByCategory, category)
                ScoreCategoryGroup(
                    categoryType = category.categoryType,
                    categoryEnglishName = category.categoryEnglishName,
                    categoryKoreanName = category.categoryKoreanName,
                    recognizedScore = recognized,
                    scores = displayScores.filter { it.category == category },
                )
            }
    }

    fun totalScoreOf(
        allScores: List<Score>,
        includeApprovedOnly: Boolean,
    ): Int {
        val target =
            if (includeApprovedOnly) {
                allScores.filter { it.scoreStatus == ScoreStatus.APPROVED }
            } else {
                allScores.filter { it.scoreStatus != ScoreStatus.REJECTED }
            }
        val grouped = target.groupBy { it.category }

        return grouped.keys
            .filterNot { it.categoryType == CategoryType.TOEIC_ACADEMY }
            .sumOf { category -> ScoreCalculatorRegistry.resolve(category.categoryType).recognizedScore(grouped, category) }
    }

    fun percentileOf(
        myUserId: Long,
        totalScoreByUserId: Map<Long, Int>,
    ): Percentile {
        val myTotalScore = totalScoreByUserId.getValue(myUserId)
        val totalCount = totalScoreByUserId.size
        val rank = totalScoreByUserId.values.count { it > myTotalScore } + 1

        val topPercentile = ((totalCount - rank + 1) * 100.0 / totalCount).roundToInt()
        val bottomPercentile = (rank * 100.0 / totalCount).roundToInt()

        return Percentile(topPercentile = topPercentile, bottomPercentile = bottomPercentile)
    }
}
```

`percentileOf`는 카테고리 계산과 무관해 변경 없이 그대로 이관. `categoryGroups`/`totalScoreOf`는 각자 흩어져 있던 `academyCategory` 탐색·필터링 코드가 사라지고 `recognizedScore(grouped, category)` 한 호출로 통일된다 — 이전에 있었던 "두 곳에서 각자 보너스 계산 중복" 문제가 자연히 해소된다.

## 7. 호출부 변경 목록

`grep -rl "ScoreCalculator\|ScoreValueConverter" src`로 확인한 전체 참조:

| 파일 | 변경 내용 |
| --- | --- |
| `domain/score/service/AppendScoreSupport.kt` | `ScoreValueConverter.convert(category, raw)` → `ScoreValueConverterRegistry.resolve(category.categoryType).convert(category, raw)`. import 변경 |
| `domain/score/service/AppendMyScoreWithValueService.kt` | `ScoreValueConverter.validateAcademicGradeRange(studentGrade, rawValue)` → `ScoreValueConverterRegistry.resolve(categoryType).validate(rawValue, studentGrade)`. import 변경 |
| `domain/score/service/FetchMyPercentInClassService.kt` | `ScoreCalculator.totalScoreOf`/`percentileOf` → `ScoreAggregator.totalScoreOf`/`percentileOf`. import 변경 |
| `domain/score/service/FetchTotalScoreService.kt` | `ScoreCalculator.totalScoreOf` → `ScoreAggregator.totalScoreOf`. import 변경 |
| `domain/score/service/FetchScoresByCategoryService.kt` | `ScoreCalculator.categoryGroups` → `ScoreAggregator.categoryGroups`. import 변경 |
| `domain/score/service/FetchMyTotalScoreService.kt` | `ScoreCalculator.totalScoreOf` → `ScoreAggregator.totalScoreOf`. import 변경 + KDoc 참조 갱신 |
| `domain/score/service/FetchMyPercentInGradeService.kt` | `ScoreCalculator.totalScoreOf`/`percentileOf` → `ScoreAggregator.totalScoreOf`/`percentileOf`. import 변경 |
| `domain/score/service/FetchMyScoresByCategoryService.kt` | `ScoreCalculator.categoryGroups` → `ScoreAggregator.categoryGroups`. import 변경 + KDoc 참조 갱신 |
| `domain/category/CategoryType.kt` | KDoc 주석의 `[team.incube.gsmc.domain.score.ScoreCalculator]` 참조를 `[team.incube.gsmc.domain.score.ScoreAggregator]`로 갱신 |

7개 서비스 모두 로직 변경 없이 **import + 호출 대상 이름만** 바뀐다. `AppendScoreSupportTest.kt`/`AppendMyScoreWithValueServiceTest.kt`는 `ScoreValueConverter`/`ScoreCalculator`를 직접 참조하지 않으므로(서비스 레이어를 거쳐 간접 호출) 수정 불필요 — 리팩터링이 behavior-preserving이면 그대로 통과해야 한다.

## 8. 테스트 마이그레이션

### `ScoreCalculatorTest.kt` 분해

- `ToeicScoreCalculatorTest.kt` (`domain/score/calculator/`) — 기존 "토익사관학교 가산점" 테스트 케이스를 `ToeicScoreCalculator().recognizedScore(mapOf(toeicCategory to toeicScores, academyCategory to academyScores), toeicCategory)` 형태로 순수 단위 테스트화. 8점+승인 보너스=9, 9점+승인 보너스가 캡(10)에서 멈춤, 미승인 시 미가산 등 기존 케이스 유지
- `ScoreAggregatorTest.kt` (`domain/score/`) — `categoryGroups()`가 `TOEIC_ACADEMY` 그룹을 결과에서 제외하는지, `totalScoreOf()`가 중복 합산하지 않는지 등 오케스트레이션 레벨 테스트 유지
- `ScoreCalculatorRegistryTest.kt` (`domain/score/calculator/`) — `TOEIC`은 `ToeicScoreCalculator`, 나머지는 `DefaultScoreCalculator` 인스턴스를 반환하는지 검증

### `ScoreValueConverterTest.kt` 분해

- `DivisorScoreValueConverterTest.kt` — TOPCIT/TOEIC/NEWRROW_SCHOOL 변환 케이스
- `AcademicGradeScoreValueConverterTest.kt` — ACADEMIC_GRADE/NCS 변환 케이스 + `validate()` 검증 케이스(학년별 범위) 전부 이관
- `DefaultScoreValueConverterTest.kt` — VOLUNTEER 등 패스스루 케이스
- `ScoreValueConverterRegistryTest.kt` — 카테고리 타입별로 올바른 컨버터 인스턴스가 반환되는지 검증

## 9. 열린 이슈 (의도적으로 범위 밖으로 둔 것)

- 레지스트리가 `else -> default` 폴백을 쓰기 때문에, 앞으로 `CategoryType`에 값이 추가되면 **컴파일러가 강제하지 않고 조용히 `default`/`DefaultScoreValueConverter`로 처리된다.** 새 카테고리가 실제로 특수 계산이 필요한 경우 사람이 카테고리 추가 체크리스트(시드 데이터, GraphQL enum 동기화 등)에서 함께 챙겨야 한다 — 컴파일타임 강제보다 가독성을 우선한 의도적 트레이드오프
- `bonusScore`/`validate` hook은 현재 TOEIC/ACADEMIC_GRADE 단 하나씩만 실질적으로 오버라이드한다. 나머지 카테고리 입장에서는 "존재하지만 항상 기본값을 반환하는" 죽은 파라미터를 갖게 되는데, 지금 시점에는 감내할 만한 트레이드오프로 판단했다
- `domain/score`에 서브패키지(`calculator/`, `converter/`)를 도입하는 것은 이 프로젝트 domain 레이어 전체에서 첫 사례다. 다른 도메인에도 같은 패턴을 적용할지는 이번 범위 밖

## 10. 작업 순서 (체크리스트)

1. [x] `domain/score/calculator/ScoreCalculator.kt` (abstract class) 작성
2. [x] `DefaultScoreCalculator.kt`, `ToeicScoreCalculator.kt` 작성
3. [x] `ScoreCalculatorRegistry.kt` 작성 (`if`로 TOEIC/JLPT만 분기, 나머지 default)
4. [x] `domain/score/converter/ScoreValueConverter.kt` (abstract class) 작성
5. [x] `DefaultScoreValueConverter.kt`, `DivisorScoreValueConverter.kt`, `AcademicGradeScoreValueConverter.kt` 작성
6. [x] `ScoreValueConverterRegistry.kt` 작성 (`when`으로 divisor/academicGrade 그룹만 분기, 나머지 else default)
7. [x] `domain/score/ScoreAggregator.kt` 작성 (기존 `categoryGroups`/`totalScoreOf`/`percentileOf` 이관)
8. [x] 기존 `domain/score/ScoreCalculator.kt`, `domain/score/ScoreValueConverter.kt` 삭제
9. [x] §7 표의 9개 파일 import/호출부 수정
10. [x] §8에 따라 기존 테스트 2개 파일을 7개 파일로 분해·재작성 (calculator 3개 + converter 4개)
11. [x] KtLint 포맷 적용 (`ktlintFormat`/`ktlintCheck` 통과)
12. [x] 전체 테스트 스위트 실행 — `./gradlew test` BUILD SUCCESSFUL, 신규 테스트 26건 전부 통과, 기존 테스트 실패 없음 (behavior-preserving 검증 완료)
