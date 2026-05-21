@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.port.`in`

/**
 * 로그아웃 유스케이스 인터페이스입니다.
 * 인바운드 포트로서, 사용자의 리프레시 토큰을 만료시켜 로그아웃 처리하는 계약을 정의합니다.
 * [LogoutService]가 이 인터페이스를 구현하여 실제 로직을 수행합니다.
 */
interface LogoutUseCase {
    /**
     * 사용자의 리프레시 토큰을 삭제하여 로그아웃 처리한다.
     *
     * @param userId 로그아웃할 사용자 ID
     */
    fun execute(userId: Long)
}
