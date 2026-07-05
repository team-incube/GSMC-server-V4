package team.incube.gsmc.domain.category.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType

/**
 * 역량 평가 카테고리 엔티티
 *
 * 학생 역량을 평가하는 카테고리 항목을 정의한다.
 * [isAccumulated]가 `true`이면 점수가 누적 합산되고, `false`이면 최신 값으로 대체된다.
 *
 * @see EvidenceType
 */
@Entity
@Table(name = "category_tb")
class CategoryJpaEntity(
    /** 카테고리 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    val categoryId: Long = 0,
    /** 점수 산출 시 적용되는 가중치 */
    @Column(name = "weight", nullable = false)
    val weight: Int,
    /** 카테고리 영문명 */
    @Column(name = "category_english_name", nullable = false, length = 50)
    val categoryEnglishName: String,
    /** 카테고리 한글명 */
    @Column(name = "category_korean_name", nullable = false, length = 50)
    val categoryKoreanName: String,
    /** 카테고리 최대 점수 */
    @Column(name = "category_maximum_value", nullable = false)
    val categoryMaximumValue: Int,
    /** 점수 누적 여부 — `true`: 합산, `false`: 최신값 대체 */
    @Column(name = "is_accumulated", nullable = false)
    val isAccumulated: Boolean,
    /** 증빙 자료 제출 방식 */
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 20)
    val evidenceType: EvidenceType,
    /** 카테고리 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 30)
    val categoryType: CategoryType,
    /** 점수 집계 방식 */
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    val calculationType: ScoreCalculationType,
)
