@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.developer.port.`in`

/**
 * 회원 탈퇴 처리 유스케이스 인터페이스입니다.
 */
interface RemoveMemberUseCase {
    /**
     * 대상 회원을 삭제한다. 근거 자료·점수·파일 등 참조 데이터가 하나라도 있으면 삭제하지 않는다.
     *
     * @param memberId 삭제할 회원 ID
     * @return 성공 여부
     * @throws team.incube.gsmc.global.exception.GsmcException 호출자가 ROOT가 아니면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     * @throws team.incube.gsmc.global.exception.GsmcException 대상 회원이 없으면 [team.incube.gsmc.global.exception.ErrorCode.USER_NOT_FOUND]
     * @throws team.incube.gsmc.global.exception.GsmcException 참조 데이터가 있으면 [team.incube.gsmc.global.exception.ErrorCode.USER_HAS_RELATED_DATA]
     */
    fun execute(memberId: Long): Boolean
}
