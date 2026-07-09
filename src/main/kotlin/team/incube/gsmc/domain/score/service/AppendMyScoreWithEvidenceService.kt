package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.AppendMyScoreWithEvidenceUseCase
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 증빙자료(텍스트) 기반 점수 추가 유스케이스 구현 클래스입니다.
 * [AppendMyScoreWithEvidenceUseCase]를 구현하며, 증빙 방식이 EVIDENCE인 카테고리에 대해 활동
 * 내용을 텍스트로 입력해 점수를 신청한다. `PROJECT_PARTICIPATION`은 DataGSM 연동 전용 플로우로만
 * 신청 가능해 이 뮤테이션에서는 명시적으로 막는다.
 */
@Port(direction = PortDirection.INBOUND)
class AppendMyScoreWithEvidenceService(
    private val appendScoreSupport: AppendScoreSupport,
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : AppendMyScoreWithEvidenceUseCase {
    @Transactional
    override fun execute(
        categoryType: CategoryType,
        value: String?,
    ): Score {
        if (categoryType == CategoryType.PROJECT_PARTICIPATION) {
            throw GsmcException(ErrorCode.INVALID_CATEGORY_TYPE)
        }

        val userId = memberUtil.getCurrentUserId()
        val category = appendScoreSupport.resolveCategory(categoryType, EvidenceType.EVIDENCE)

        val target = appendScoreSupport.findOrCreateScore(userId, category)
        return scorePersistencePort.save(
            target.copy(
                scoreStatus = ScoreStatus.PENDING,
                activityName = value,
                scoreValue = null,
                rejectionReason = null,
            ),
        )
    }
}
