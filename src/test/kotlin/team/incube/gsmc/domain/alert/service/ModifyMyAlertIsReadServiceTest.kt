package team.incube.gsmc.domain.alert.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.AlertType
import team.incube.gsmc.domain.alert.port.out.AlertPersistencePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class ModifyMyAlertIsReadServiceTest :
    BehaviorSpec({
        val alertPersistencePort = mockk<AlertPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = ModifyMyAlertIsReadService(alertPersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun alert(
            alertId: Long,
            userId: Long,
        ) = Alert(
            alertId = alertId,
            userId = userId,
            scoreId = null,
            alertType = AlertType.APPROVED,
            content = "내용",
            isRead = false,
            createdAt = LocalDateTime.now(),
        )

        Given("로그인한 사용자가") {
            When("본인의 lastAlertId까지 읽음 처리하면") {
                Then("이하의 미읽음 알림을 일괄 읽음 처리한다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(5L) } returns alert(5L, 10L)
                    every { alertPersistencePort.markAsReadUpTo(10L, 5L) } just runs

                    val result = service.execute(5L)

                    result shouldBe true
                    verify(exactly = 1) { alertPersistencePort.markAsReadUpTo(10L, 5L) }
                }
            }

            When("이미 읽은 알림까지 다시 읽음 처리하면") {
                Then("같은 결과로 멱등적으로 처리된다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(5L) } returns alert(5L, 10L)
                    every { alertPersistencePort.markAsReadUpTo(10L, 5L) } just runs

                    service.execute(5L)
                    val result = service.execute(5L)

                    result shouldBe true
                    verify(exactly = 2) { alertPersistencePort.markAsReadUpTo(10L, 5L) }
                }
            }

            When("다른 사용자의 lastAlertId로 읽음 처리를 시도하면") {
                Then("ALERT_NOT_FOUND 예외가 발생하고 일괄 갱신은 호출되지 않는다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(99L) } returns alert(99L, 999L)

                    val exception = shouldThrow<GsmcException> { service.execute(99L) }

                    exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
                    verify(exactly = 0) { alertPersistencePort.markAsReadUpTo(any(), any()) }
                }
            }

            When("존재하지 않는 lastAlertId로 읽음 처리를 시도하면") {
                Then("ALERT_NOT_FOUND 예외가 발생한다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L) }

                    exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
                }
            }
        }
    })
