package team.incube.gsmc.domain.alert.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.port.`in`.FetchMyAlertsUseCase
import team.incube.gsmc.domain.alert.port.out.AlertPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 알림 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchMyAlertsUseCase]를 구현하며, 현재 로그인한 사용자가 수신한 알림을 최신순으로 조회합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyAlertsService(
    private val alertPersistencePort: AlertPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyAlertsUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Alert> {
        val userId = memberUtil.getCurrentUserId()
        return alertPersistencePort.findAllByUserIdOrderByCreatedAtDesc(userId)
    }
}
