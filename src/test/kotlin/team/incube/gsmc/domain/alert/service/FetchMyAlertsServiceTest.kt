package team.incube.gsmc.domain.alert.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.AlertType
import team.incube.gsmc.domain.alert.port.out.AlertPersistencePort
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class FetchMyAlertsServiceTest :
    BehaviorSpec({
        val alertPersistencePort = mockk<AlertPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchMyAlertsService(alertPersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun alert(
            alertId: Long,
            userId: Long = 10L,
        ) = Alert(
            alertId = alertId,
            userId = userId,
            scoreId = 1L,
            alertType = AlertType.APPROVED,
            content = "수상경력 점수가 승인되었습니다.",
            isRead = false,
            createdAt = LocalDateTime.now(),
        )

        Given("로그인한 사용자가") {
            When("알림 목록을 조회하면") {
                Then("현재 사용자의 알림을 최신순으로 반환한다") {
                    val alerts = listOf(alert(2L), alert(1L))
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findAllByUserIdOrderByCreatedAtDesc(10L) } returns alerts

                    val result = service.execute()

                    result shouldBe alerts
                    verify(exactly = 1) { alertPersistencePort.findAllByUserIdOrderByCreatedAtDesc(10L) }
                }
            }

            When("수신한 알림이 없으면") {
                Then("빈 목록을 반환한다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findAllByUserIdOrderByCreatedAtDesc(10L) } returns emptyList()

                    val result = service.execute()

                    result.shouldBeEmpty()
                }
            }
        }
    })
