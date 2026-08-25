package team.incube.gsmc.domain.category.adapter.web

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType

/**
 * 카테고리 조회/검색 Query의 GraphQL 응답 DTO입니다.
 * 도메인 [Category]와 필드명이 달라(`categoryEnglishName` → `englishName` 등) 별도로 매핑합니다.
 */
data class CategoryPayload(
    val categoryType: CategoryType,
    val englishName: String,
    val koreanName: String,
    val weight: Int,
    val maxRecordCount: Int,
    val evidenceType: EvidenceType,
    val calculationType: ScoreCalculationType,
)

/**
 * 도메인 모델 [Category]를 [CategoryPayload]로 변환한다.
 *
 * @receiver 변환할 도메인 객체
 * @return 변환된 [CategoryPayload]
 */
fun Category.toPayload(): CategoryPayload =
    CategoryPayload(
        categoryType = categoryType,
        englishName = categoryEnglishName,
        koreanName = categoryKoreanName,
        weight = weight,
        maxRecordCount = categoryMaximumValue,
        evidenceType = evidenceType,
        calculationType = calculationType,
    )
