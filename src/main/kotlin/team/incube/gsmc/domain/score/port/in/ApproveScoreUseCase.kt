@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

/**
 * 점수 승인 유스케이스 인터페이스입니다. 교사(TEACHER) 이상만 호출할 수 있다.
 */
interface ApproveScoreUseCase {
    /**
     * @param scoreId 승인할 점수 요청 ID
     * @return 처리 성공 여부
     */
    fun execute(scoreId: Long): Boolean
}
