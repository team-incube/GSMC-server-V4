package team.incube.gsmc.domain.score

import team.incube.gsmc.domain.category.CategoryType

/**
 * 카테고리별로 묶은 점수 요청 그룹
 *
 * @param categoryType 카테고리 유형
 * @param categoryEnglishName 카테고리 영문명
 * @param categoryKoreanName 카테고리 한글명
 * @param recognizedScore 해당 카테고리의 인정 점수
 * @param scores 해당 카테고리에 속한 점수 요청 목록
 */
data class ScoreCategoryGroup(
    val categoryType: CategoryType,
    val categoryEnglishName: String,
    val categoryKoreanName: String,
    val recognizedScore: Int,
    val scores: List<Score>,
)
