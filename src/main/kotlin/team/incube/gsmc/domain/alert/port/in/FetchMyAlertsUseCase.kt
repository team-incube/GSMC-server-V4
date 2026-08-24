@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.alert.port.`in`

import team.incube.gsmc.domain.alert.Alert

/**
 * 내 알림 목록 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyAlertsUseCase {
    /**
     * @return 현재 로그인한 사용자가 수신한 알림 목록, 최신순
     */
    fun execute(): List<Alert>
}
