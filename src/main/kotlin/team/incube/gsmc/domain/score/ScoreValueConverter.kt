package team.incube.gsmc.domain.score

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import kotlin.math.roundToInt

/**
 * 제출된 원점수를 카테고리별 인정점수로 환산하는 순수 도메인 로직입니다.
 * I/O가 없는 계산 전용 객체로, 서비스 계층이 값 파싱 직후 호출해 [team.incube.gsmc.domain.score.Score.scoreValue]에
 * 저장할 값을 만든다.
 */
object ScoreValueConverter {
    /** 2026학년도 기준 5등급제를 쓰는 학년(1·2학년), 3학년은 9등급제 */
    private val FIVE_GRADE_SCALE_STUDENT_GRADES = setOf(1, 2)

    /**
     * [category]의 환산 규칙에 따라 원점수 [rawValue]를 인정점수로 변환한다.
     *
     * - TOPCIT/TOEIC(JLPT 포함, TOEIC 행 공유)/뉴로우스쿨: [Category.conversionDivisor]로 나눈 뒤 반올림
     * - 교과성적/NCS: [Category.categoryMaximumValue] + 1에서 등급을 뺀 값 (등급이 낮을수록 고득점)
     * - 그 외: 변환 없이 반올림한 원점수 그대로
     *
     * @param category 대상 카테고리
     * @param rawValue 파싱된 원점수
     * @return 인정점수로 환산된 값
     */
    fun convert(
        category: Category,
        rawValue: Double,
    ): Int =
        when (category.categoryType) {
            CategoryType.TOPCIT, CategoryType.TOEIC, CategoryType.NEWRROW_SCHOOL ->
                (rawValue / category.conversionDivisor).roundToInt()
            CategoryType.ACADEMIC_GRADE, CategoryType.NCS ->
                (category.categoryMaximumValue + 1) - rawValue.roundToInt()
            else -> rawValue.roundToInt()
        }

    /**
     * 교과성적 제출값이 학생의 등급제 범위를 벗어나지 않는지 검증한다.
     * 2026학년도 기준 1·2학년은 5등급제(1~5), 3학년은 9등급제(1~9)를 쓴다.
     *
     * @param studentGrade 학생의 학년(1~3)
     * @param rawValue 제출된 평균 등급
     * @throws GsmcException 학년별 유효 범위를 벗어나면 [ErrorCode.INVALID_SCORE_VALUE]
     */
    fun validateAcademicGradeRange(
        studentGrade: Int,
        rawValue: Double,
    ) {
        val maxValidGrade = if (studentGrade in FIVE_GRADE_SCALE_STUDENT_GRADES) 5 else 9
        if (rawValue.roundToInt() !in 1..maxValidGrade) {
            throw GsmcException(ErrorCode.INVALID_SCORE_VALUE)
        }
    }
}
