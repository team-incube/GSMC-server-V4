@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.score.Percentile

/**
 * 학년 내 백분위 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyPercentInGradeUseCase {
    /**
     * 현재 로그인한 학생의 같은 학년 내 백분위를 계산한다. 학생(STUDENT)만 호출할 수 있다.
     *
     * @param includeApprovedOnly true면 승인된 점수만 합산, false면 전체 상태 포함
     * @return 학년 내 백분위
     * @throws team.incube.gsmc.global.exception.GsmcException 호출자가 학생이 아니면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     */
    fun execute(includeApprovedOnly: Boolean): Percentile
}
