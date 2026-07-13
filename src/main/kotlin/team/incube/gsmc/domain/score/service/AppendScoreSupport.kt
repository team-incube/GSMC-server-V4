package team.incube.gsmc.domain.score.service

import org.springframework.stereotype.Component
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.time.LocalDateTime

/**
 * 4개 범용 점수 추가 서비스([AppendMyScoreWithFileService] 등)가 공유하는 카테고리 검증, 값 파싱,
 * 반려 후 재제출 판단 로직을 모아둔 헬퍼입니다. 포트가 아닌 순수 협력 객체로, 여러 서비스에 그대로 주입된다.
 */
@Component
class AppendScoreSupport(
    private val categoryPersistencePort: CategoryPersistencePort,
    private val scorePersistencePort: ScorePersistencePort,
) {
    /**
     * [categoryType]으로 카테고리를 조회하고 증빙 방식이 [requiredEvidenceType]인지 검증한다.
     *
     * @throws GsmcException 카테고리가 없으면 [ErrorCode.CATEGORY_NOT_FOUND]
     * @throws GsmcException 증빙 방식이 일치하지 않으면 [ErrorCode.INVALID_CATEGORY_TYPE]
     */
    fun resolveCategory(
        categoryType: CategoryType,
        requiredEvidenceType: EvidenceType,
    ): Category {
        val category = findCategoryOrThrow(categoryType)
        if (category.evidenceType != requiredEvidenceType) {
            throw GsmcException(ErrorCode.INVALID_CATEGORY_TYPE)
        }
        return category
    }

    /**
     * [categoryType]으로 카테고리를 조회하고 증빙 방식이 UNREQUIRED이면서 집계 방식이
     * [requiredCalculationType]인지 검증한다 (`addScoreWithValue`/`addScoreOnly` 전용).
     *
     * @throws GsmcException 카테고리가 없으면 [ErrorCode.CATEGORY_NOT_FOUND]
     * @throws GsmcException 조건이 일치하지 않으면 [ErrorCode.INVALID_CATEGORY_TYPE]
     */
    fun resolveUnrequiredCategory(
        categoryType: CategoryType,
        requiredCalculationType: ScoreCalculationType,
    ): Category {
        val category = findCategoryOrThrow(categoryType)
        if (category.evidenceType != EvidenceType.UNREQUIRED || category.calculationType != requiredCalculationType) {
            throw GsmcException(ErrorCode.INVALID_CATEGORY_TYPE)
        }
        return category
    }

    /**
     * 문자열 [value]를 정수 점수 값으로 파싱한다.
     *
     * @throws GsmcException 파싱할 수 없으면 [ErrorCode.INVALID_SCORE_VALUE]
     */
    fun parseScoreValue(value: String?): Int =
        value?.trim()?.toIntOrNull() ?: throw GsmcException(ErrorCode.INVALID_SCORE_VALUE)

    /**
     * 재사용할 기존 `REJECTED` 점수 요청이 있으면 그것을, 없으면 새로 저장할 빈 [Score]를 반환한다.
     * 호출부는 반환된 객체를 `copy()`해서 실제 값을 채운 뒤 저장한다.
     *
     * 누적 카테고리([Category.isAccumulated]=true, 예: 자격증)는 같은 카테고리에 여러 건이 동시에
     * 존재할 수 있어(취득한 자격증마다 별개의 제출) `REJECTED` row가 여러 개일 수 있다 — 카테고리
     * 단위로 하나만 재사용하면 다른 제출을 덮어쓰고, `fetchOne()`이 `NonUniqueResultException`을
     * 던질 수도 있다. 그래서 누적 카테고리는 항상 새 [Score]를 생성하고, 비누적 카테고리에서만
     * 재사용한다(비누적 카테고리는 카테고리당 제출이 항상 1건이므로 안전).
     *
     * @param userId 현재 사용자 ID
     * @param category 대상 카테고리
     * @return 재사용 또는 신규 생성용 [Score]
     */
    fun findOrCreateScore(
        userId: Long,
        category: Category,
    ): Score {
        if (!category.isAccumulated) {
            scorePersistencePort
                .findByUserIdAndCategoryTypeAndScoreStatus(
                    userId,
                    category.categoryType,
                    ScoreStatus.REJECTED,
                )?.let {
                    return it
                }
        }

        val now = LocalDateTime.now()
        return Score(
            scoreId = 0L,
            userId = userId,
            category = category,
            evidence = null,
            file = null,
            scoreStatus = ScoreStatus.PENDING,
            activityName = null,
            scoreValue = null,
            rejectionReason = null,
            dgProjectId = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun findCategoryOrThrow(categoryType: CategoryType): Category =
        categoryPersistencePort.findByCategoryType(categoryType) ?: throw GsmcException(ErrorCode.CATEGORY_NOT_FOUND)
}
