package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.score.ScoreCalculator
import team.incube.gsmc.domain.score.ScoreCategoryGroup
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.FetchScoresByCategoryUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.canAccessScoresOf
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 특정 사용자 카테고리별 점수 조회 유스케이스 구현 클래스입니다.
 * [FetchScoresByCategoryUseCase]를 구현하며, [team.incube.gsmc.domain.user.canAccessScoresOf]로 판단한
 * 접근 권한이 있는 경우에만 호출을 허용합니다 (담임 교사는 본인 담당 학급 학생만 조회 가능).
 */
@Port(direction = PortDirection.INBOUND)
class FetchScoresByCategoryService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchScoresByCategoryUseCase {
    override fun execute(
        memberId: Long,
        status: ScoreStatus?,
    ): List<ScoreCategoryGroup> {
        val target = memberPersistencePort.findByUserId(memberId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        val currentUser =
            memberPersistencePort.findByUserId(memberUtil.getCurrentUserId())
                ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        if (!currentUser.canAccessScoresOf(target)) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val scores = scorePersistencePort.findAllByUserId(memberId)
        return ScoreCalculator.categoryGroups(scores, status)
    }
}
