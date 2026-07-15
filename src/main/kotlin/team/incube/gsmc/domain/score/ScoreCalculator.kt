package team.incube.gsmc.domain.score

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.ScoreCalculationType
import kotlin.math.roundToInt

/**
 * 점수 요청 목록으로부터 인정 점수·총점·백분위를 계산하는 순수 도메인 로직입니다.
 * I/O가 없는 계산 전용 객체로, 서비스 계층에서 [team.incube.gsmc.domain.score.port.out.ScorePersistencePort]로
 * 조회한 [Score] 목록을 넘겨받아 사용한다.
 */
object ScoreCalculator {
    /**
     * 한 카테고리에 속한 점수 요청 목록으로 해당 카테고리의 인정 점수를 계산한다.
     * [ScoreCalculationType.COUNT_BASED]는 건수, [ScoreCalculationType.SCORE_BASED]는 [Score.scoreValue] 합산을
     * 원점수로 하며, [Category.isAccumulated] 여부에 따라 누적 합산할지 최신값만 반영할지가 갈린다.
     * 이후 [Category.categoryMaximumValue]로 캡핑하고 [Category.weight]를 곱한다.
     *
     * @param scoresInCategory 계산 대상 카테고리에 속한 점수 요청 목록 (이미 원하는 상태로 필터링된 상태여야 함)
     * @param category 대상 카테고리
     * @return 해당 카테고리의 인정 점수
     */
    fun recognizedScore(
        scoresInCategory: List<Score>,
        category: Category,
    ): Int = minOf(rawScoreOf(scoresInCategory, category), category.categoryMaximumValue) * category.weight

    /**
     * 카테고리의 raw 값(캡·가중치 적용 전)을 계산한다. [ScoreCalculationType.COUNT_BASED]는 건수,
     * [ScoreCalculationType.SCORE_BASED]는 [Score.scoreValue] 합산 또는 최신값을 원점수로 한다.
     */
    private fun rawScoreOf(
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

    /**
     * TOEIC(JLPT 포함) 카테고리의 인정점수를 계산하되, 토익사관학교([CategoryType.TOEIC_ACADEMY]) 참여
     * 승인 여부를 raw 값에 가산한 뒤 캡·가중치를 적용한다. 토익사관학교는 독립 카테고리로 집계되지 않고
     * 이 보너스로만 총점에 반영된다.
     */
    private fun recognizedToeicScoreWithAcademyBonus(
        toeicScores: List<Score>,
        toeicCategory: Category,
        academyScores: List<Score>,
        academyCategory: Category?,
    ): Int {
        val academyBonus = academyCategory?.let { rawScoreOf(academyScores, it) } ?: 0
        val raw = rawScoreOf(toeicScores, toeicCategory) + academyBonus
        return minOf(raw, toeicCategory.categoryMaximumValue) * toeicCategory.weight
    }

    /**
     * 점수 요청 목록을 카테고리별로 묶는다. 인정 점수는 항상 승인(APPROVED)된 점수만으로 계산하고,
     * [statusFilter]는 각 그룹의 [ScoreCategoryGroup.scores] 목록을 필터링하는 용도로만 쓰인다.
     *
     * @param allScores 대상 사용자의 전체 점수 요청 목록 (모든 상태 포함)
     * @param statusFilter 그룹의 scores 목록에 적용할 상태 필터, null이면 전체 상태 포함
     * @return 카테고리별 점수 요청 그룹 목록
     */
    fun categoryGroups(
        allScores: List<Score>,
        statusFilter: ScoreStatus?,
    ): List<ScoreCategoryGroup> {
        val approvedScores = allScores.filter { it.scoreStatus == ScoreStatus.APPROVED }
        val displayScores = if (statusFilter != null) allScores.filter { it.scoreStatus == statusFilter } else allScores

        val categories = allScores.map { it.category }.distinct()
        val academyCategory = categories.find { it.categoryType == CategoryType.TOEIC_ACADEMY }

        return categories
            .filterNot { it.categoryType == CategoryType.TOEIC_ACADEMY }
            .map { category ->
                val scoresInCategory = approvedScores.filter { it.category == category }
                val recognized =
                    if (category.categoryType == CategoryType.TOEIC) {
                        recognizedToeicScoreWithAcademyBonus(
                            scoresInCategory,
                            category,
                            approvedScores.filter { it.category == academyCategory },
                            academyCategory,
                        )
                    } else {
                        recognizedScore(scoresInCategory, category)
                    }
                ScoreCategoryGroup(
                    categoryType = category.categoryType,
                    categoryEnglishName = category.categoryEnglishName,
                    categoryKoreanName = category.categoryKoreanName,
                    recognizedScore = recognized,
                    scores = displayScores.filter { it.category == category },
                )
            }
    }

    /**
     * 점수 요청 목록으로 총점을 계산한다. 카테고리별로 묶어 인정 점수를 계산한 뒤 모두 합산한다.
     *
     * @param allScores 대상 사용자의 전체 점수 요청 목록 (모든 상태 포함)
     * @param includeApprovedOnly true면 승인된 점수만 합산, false면 반려(REJECTED)를 제외한 나머지 상태 포함
     * @return 총점
     */
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
        val academyCategory = grouped.keys.find { it.categoryType == CategoryType.TOEIC_ACADEMY }

        return grouped.entries
            .filterNot { (category, _) -> category.categoryType == CategoryType.TOEIC_ACADEMY }
            .sumOf { (category, scoresInCategory) ->
                if (category.categoryType == CategoryType.TOEIC) {
                    recognizedToeicScoreWithAcademyBonus(
                        scoresInCategory,
                        category,
                        academyCategory?.let { grouped[it].orEmpty() } ?: emptyList(),
                        academyCategory,
                    )
                } else {
                    recognizedScore(scoresInCategory, category)
                }
            }
    }

    /**
     * 모집단 내에서 본인의 백분위를 계산한다. 동점자는 표준 경기 순위(공동순위 + 다음 순위 건너뛰기)로 처리한다.
     *
     * @param myUserId 본인 사용자 ID
     * @param totalScoreByUserId 모집단 전체 사용자의 총점 (본인 포함)
     * @return topPercentile은 1등일수록 100에 가깝고, bottomPercentile은 1등일수록 낮은 값
     */
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
