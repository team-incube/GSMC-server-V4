@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.alert.port.`in`

/**
 * 내 알림 삭제 유스케이스 인터페이스입니다.
 */
interface RemoveMyAlertUseCase {
    /**
     * @param alertId 삭제할 알림 ID
     * @return 처리 성공 여부
     */
    fun execute(alertId: Long): Boolean
}
