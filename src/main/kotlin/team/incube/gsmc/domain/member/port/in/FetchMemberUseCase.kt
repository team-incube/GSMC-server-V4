@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.member.port.`in`

import team.incube.gsmc.domain.user.User

/**
 * 사용자 단건 조회 유스케이스 인터페이스입니다.
 */
interface FetchMemberUseCase {
    /**
     * ID로 사용자를 조회한다.
     *
     * @param memberId 조회할 사용자 ID
     * @return 해당 사용자
     * @throws team.incube.gsmc.global.exception.GsmcException 사용자가 없으면 [team.incube.gsmc.global.exception.ErrorCode.USER_NOT_FOUND]
     */
    fun execute(memberId: Long): User
}
