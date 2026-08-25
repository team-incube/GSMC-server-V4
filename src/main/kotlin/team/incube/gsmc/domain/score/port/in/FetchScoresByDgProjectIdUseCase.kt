@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.score.Score

/**
 * 같은 DataGSM 프로젝트로 제출한 사람 전원의 점수 요청을 모아보는 유스케이스 인터페이스입니다.
 * 교사(TEACHER) 이상만 호출할 수 있다.
 */
interface FetchScoresByDgProjectIdUseCase {
    /**
     * @param dgProjectId 조회할 DataGSM 프로젝트 ID
     * @return 해당 프로젝트로 제출된 점수 요청 목록
     */
    fun execute(dgProjectId: Long): List<Score>
}
