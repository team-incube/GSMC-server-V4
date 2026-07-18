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
