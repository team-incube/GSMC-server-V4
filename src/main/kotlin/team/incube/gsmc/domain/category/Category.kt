package team.incube.gsmc.domain.category

/**
 * 역량 평가 카테고리 도메인 모델
 *
 * 인프라 의존성 없는 순수 도메인 객체로, 서비스 계층의 비즈니스 로직이 이 객체를 통해 카테고리를 다룬다.
 * DB 연동이 필요한 경우 [team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity]로 변환한다.
 *
 * @param categoryId 카테고리 고유 식별자
 * @param weight 점수 산출 시 적용되는 가중치
 * @param categoryEnglishName 카테고리 영문명
 * @param categoryKoreanName 카테고리 한글명
 * @param categoryMaximumValue 카테고리 최대 점수
 * @param isAccumulated 점수 누적 여부 — `true`: 합산, `false`: 최신값 대체
 * @param evidenceType 증빙 자료 제출 방식
 * @param categoryType 카테고리 유형
 * @param calculationType 점수 집계 방식
 * @param conversionDivisor 원점수를 인정점수로 환산할 때 나눌 값 (예: TOPCIT/TOEIC=100, 뉴로우스쿨=20), 환산이 필요 없으면 1
 * @see EvidenceType
 * @see CategoryType
 * @see ScoreCalculationType
 */
data class Category(
    val categoryId: Long,
    val weight: Int,
    val categoryEnglishName: String,
    val categoryKoreanName: String,
    val categoryMaximumValue: Int,
    val isAccumulated: Boolean,
    val evidenceType: EvidenceType,
    val categoryType: CategoryType,
    val calculationType: ScoreCalculationType,
    val conversionDivisor: Int = 1,
)
