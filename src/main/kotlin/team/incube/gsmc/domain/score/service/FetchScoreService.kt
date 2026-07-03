package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.port.`in`.FetchScoreUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.canAccessScoresOf
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 점수 단건 조회 유스케이스 구현 클래스입니다.
 * [FetchScoreUseCase]를 구현하며, 본인 소유이거나 [team.incube.gsmc.domain.user.canAccessScoresOf]로
 * 판단한 접근 권한이 있는 경우에만 조회를 허용합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchScoreService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchScoreUseCase {
    override fun execute(scoreId: Long): Score {
        val score = scorePersistencePort.findById(scoreId) ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)

        val currentUserId = memberUtil.getCurrentUserId()
        if (score.userId != currentUserId) {
            val currentUser =
                memberPersistencePort.findByUserId(currentUserId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
            val target =
                memberPersistencePort.findByUserId(score.userId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
            if (!currentUser.canAccessScoresOf(target)) {
                throw GsmcException(ErrorCode.FORBIDDEN)
            }
        }

        return score
    }
}
