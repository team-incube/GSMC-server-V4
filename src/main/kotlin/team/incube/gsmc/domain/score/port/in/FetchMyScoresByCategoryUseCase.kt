@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.score.ScoreCategoryGroup
import team.incube.gsmc.domain.score.ScoreStatus

/**
 * 내 카테고리별 점수 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyScoresByCategoryUseCase {
    /**
     * 현재 로그인한 사용자의 점수 요청을 카테고리별로 묶어 조회한다.
     *
     * @param status 상태 필터, null이면 전체
     * @return 카테고리별 점수 요청 그룹 목록
     */
    fun execute(status: ScoreStatus?): List<ScoreCategoryGroup>
}
