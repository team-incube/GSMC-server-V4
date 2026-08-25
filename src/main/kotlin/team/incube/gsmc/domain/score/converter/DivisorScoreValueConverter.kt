package team.incube.gsmc.domain.score.converter

import team.incube.gsmc.domain.category.Category
import kotlin.math.roundToInt

class DivisorScoreValueConverter : ScoreValueConverter() {
    override fun convert(
        category: Category,
        rawValue: Double,
    ): Int = (rawValue / category.conversionDivisor).roundToInt()
}
