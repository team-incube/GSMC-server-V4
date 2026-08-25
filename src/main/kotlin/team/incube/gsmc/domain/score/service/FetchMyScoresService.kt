package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.FetchMyScoresUseCase
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 점수 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchMyScoresUseCase]를 구현하며, 현재 로그인한 사용자의 점수 요청을 [ScorePersistencePort]로 조회한 뒤
 * categoryType/status로 필터링합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyScoresService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyScoresUseCase {
    override fun execute(
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): List<Score> {
        val userId = memberUtil.getCurrentUserId()
        return scorePersistencePort
            .findAllByUserId(userId)
            .filter { categoryType == null || it.category.categoryType == categoryType }
            .filter { status == null || it.scoreStatus == status }
    }
}
