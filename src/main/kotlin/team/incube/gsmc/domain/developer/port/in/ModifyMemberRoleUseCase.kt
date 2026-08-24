@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.developer.port.`in`

import team.incube.gsmc.domain.user.UserRole

/**
 * 회원 역할 변경 유스케이스 인터페이스입니다.
 */
interface ModifyMemberRoleUseCase {
    /**
     * 대상 회원의 역할을 변경한다.
     *
     * @param email 역할을 변경할 회원의 이메일
     * @param role 변경할 역할
     * @return 성공 여부
     * @throws team.incube.gsmc.global.exception.GsmcException 호출자가 ROOT가 아니면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     * @throws team.incube.gsmc.global.exception.GsmcException 대상 회원이 없으면 [team.incube.gsmc.global.exception.ErrorCode.USER_NOT_FOUND]
     */
    fun execute(
        email: String,
        role: UserRole,
    ): Boolean
}
