package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.score.ScoreCalculator
import team.incube.gsmc.domain.score.TotalScore
import team.incube.gsmc.domain.score.port.`in`.FetchTotalScoreUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.canAccessScoresOf
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 특정 사용자 총점 조회 유스케이스 구현 클래스입니다.
 * [FetchTotalScoreUseCase]를 구현하며, [team.incube.gsmc.domain.user.canAccessScoresOf]로 판단한
 * 접근 권한이 있는 경우에만 호출을 허용합니다 (담임 교사는 본인 담당 학급 학생만 조회 가능).
 */
@Port(direction = PortDirection.INBOUND)
class FetchTotalScoreService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchTotalScoreUseCase {
    override fun execute(
        memberId: Long,
        includeApprovedOnly: Boolean,
    ): TotalScore {
        val target = memberPersistencePort.findByUserId(memberId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        val currentUser =
            memberPersistencePort.findByUserId(memberUtil.getCurrentUserId())
                ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        if (!currentUser.canAccessScoresOf(target)) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val scores = scorePersistencePort.findAllByUserId(memberId)
        return TotalScore(ScoreCalculator.totalScoreOf(scores, includeApprovedOnly))
    }
}
