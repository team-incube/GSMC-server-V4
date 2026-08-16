@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.member.port.`in`

import team.incube.gsmc.domain.user.User

/**
 * 현재 로그인 사용자 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyMemberUseCase {
    /**
     * 현재 로그인한 사용자를 조회한다.
     *
     * @return 현재 로그인한 사용자
     */
    fun execute(): User
}
