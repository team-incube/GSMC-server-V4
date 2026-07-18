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
