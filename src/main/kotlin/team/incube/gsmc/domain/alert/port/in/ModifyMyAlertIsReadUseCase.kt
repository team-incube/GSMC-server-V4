@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.alert.port.`in`

/**
 * 내 알림 읽음 처리 유스케이스 인터페이스입니다.
 */
interface ModifyMyAlertIsReadUseCase {
    /**
     * 현재 로그인한 사용자의 알림 중 [lastAlertId] 이하의 미읽음 알림을 일괄 읽음 처리한다.
     *
     * @param lastAlertId 이 ID 이하의 알림을 읽음 처리한다
     * @return 처리 성공 여부
     */
    fun execute(lastAlertId: Long): Boolean
}
