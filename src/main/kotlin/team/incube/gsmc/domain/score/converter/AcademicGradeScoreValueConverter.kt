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
