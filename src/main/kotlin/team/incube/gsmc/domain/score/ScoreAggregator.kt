package team.incube.gsmc.domain.score

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
                val recognized =
                    ScoreCalculatorRegistry
                        .resolve(
                            category.categoryType,
                        ).recognizedScore(approvedByCategory, category)
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
            .sumOf { category ->
                ScoreCalculatorRegistry.resolve(category.categoryType).recognizedScore(grouped, category)
            }
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
