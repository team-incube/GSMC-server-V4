@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus

/**
 * 내 점수 목록 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyScoresUseCase {
    /**
     * 현재 로그인한 사용자의 점수 요청 목록을 조회한다.
     *
     * @param categoryType 카테고리 필터, null이면 전체
     * @param status 상태 필터, null이면 전체
     * @return 조건에 맞는 점수 요청 목록
     */
    fun execute(
        categoryType: CategoryType?,
        status: ScoreStatus?,
    ): List<Score>
}
