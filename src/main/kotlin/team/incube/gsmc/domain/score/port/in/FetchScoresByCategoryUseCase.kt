@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.score.ScoreCategoryGroup
import team.incube.gsmc.domain.score.ScoreStatus

/**
 * 특정 사용자 카테고리별 점수 조회 유스케이스 인터페이스입니다.
 */
interface FetchScoresByCategoryUseCase {
    /**
     * 특정 사용자의 점수 요청을 카테고리별로 묶어 조회한다. 교사(TEACHER) 이상만 호출할 수 있다.
     *
     * @param memberId 조회 대상 사용자 ID
     * @param status 상태 필터, null이면 전체
     * @return 카테고리별 점수 요청 그룹 목록
     * @throws team.incube.gsmc.global.exception.GsmcException 대상 사용자가 없으면 [team.incube.gsmc.global.exception.ErrorCode.USER_NOT_FOUND],
     * 권한이 없으면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     */
    fun execute(
        memberId: Long,
        status: ScoreStatus?,
    ): List<ScoreCategoryGroup>
}
