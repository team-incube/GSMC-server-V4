package team.incube.gsmc.domain.score.calculator

import team.incube.gsmc.domain.category.CategoryType

object ScoreCalculatorRegistry {
    private val default = DefaultScoreCalculator()
    private val toeic = ToeicScoreCalculator()

    fun resolve(categoryType: CategoryType): ScoreCalculator =
        when (categoryType) {
            CategoryType.TOEIC, CategoryType.JLPT -> toeic
            else -> default
        }
}
