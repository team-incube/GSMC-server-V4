package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.score.ScoreAggregator
import team.incube.gsmc.domain.score.ScoreCategoryGroup
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.FetchMyScoresByCategoryUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 카테고리별 점수 조회 유스케이스 구현 클래스입니다.
 * [FetchMyScoresByCategoryUseCase]를 구현하며, [ScoreAggregator.categoryGroups]로 카테고리별 그룹을 계산합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyScoresByCategoryService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyScoresByCategoryUseCase {
    override fun execute(status: ScoreStatus?): List<ScoreCategoryGroup> {
        val userId = memberUtil.getCurrentUserId()
        val me = memberPersistencePort.findByUserId(userId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        val scores = scorePersistencePort.findAllByUserId(userId)
        return ScoreAggregator.categoryGroups(scores, status, me.userGrade)
    }
}
