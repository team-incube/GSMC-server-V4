@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.score.Score

/**
 * 점수 단건 조회 유스케이스 인터페이스입니다.
 */
interface FetchScoreUseCase {
    /**
     * ID로 점수 요청 단건을 조회한다. 본인 소유이거나 교사(TEACHER) 이상만 조회할 수 있다.
     *
     * @param scoreId 조회할 점수 요청 ID
     * @return 해당 점수 요청
     * @throws team.incube.gsmc.global.exception.GsmcException 점수가 없으면 [team.incube.gsmc.global.exception.ErrorCode.SCORE_NOT_FOUND],
     * 접근 권한이 없으면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     */
    fun execute(scoreId: Long): Score
}
