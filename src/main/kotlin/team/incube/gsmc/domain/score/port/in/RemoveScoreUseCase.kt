@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

/**
 * 점수 삭제 유스케이스 인터페이스입니다. 교사(TEACHER) 이상만 호출할 수 있으며, 심사 상태와
 * 무관하게 하드 삭제한다.
 */
interface RemoveScoreUseCase {
    /**
     * @param scoreId 삭제할 점수 요청 ID
     * @return 처리 성공 여부
     */
    fun execute(scoreId: Long): Boolean
}
