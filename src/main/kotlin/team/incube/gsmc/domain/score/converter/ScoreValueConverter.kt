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
    ) {
        // 기본적으로 검증하지 않음. 필요한 카테고리만 오버라이드해서 검증 로직을 추가한다.
    }
}
