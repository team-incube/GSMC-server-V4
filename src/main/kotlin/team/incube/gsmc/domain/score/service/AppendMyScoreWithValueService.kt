package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.AppendMyScoreWithValueUseCase
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 값 기반 점수 추가 유스케이스 구현 클래스입니다.
 * [AppendMyScoreWithValueUseCase]를 구현하며, 증빙이 필요 없고 집계 방식이 SCORE_BASED인
 * 카테고리에 대해 숫자 값을 입력해 점수를 신청한다.
 */
@Port(direction = PortDirection.INBOUND)
class AppendMyScoreWithValueService(
    private val appendScoreSupport: AppendScoreSupport,
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : AppendMyScoreWithValueUseCase {
    @Transactional
    override fun execute(
        categoryType: CategoryType,
        value: String?,
    ): Score {
        val userId = memberUtil.getCurrentUserId()
        val category = appendScoreSupport.resolveUnrequiredCategory(categoryType, ScoreCalculationType.SCORE_BASED)
        val scoreValue = appendScoreSupport.parseScoreValue(value)

        val target = appendScoreSupport.findOrCreateScore(userId, category)
        return scorePersistencePort.save(
            target.copy(
                scoreStatus = ScoreStatus.PENDING,
                activityName = null,
                scoreValue = scoreValue,
                rejectionReason = null,
            ),
        )
    }
}
