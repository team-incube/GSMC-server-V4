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

class RemoveMyAlertServiceTest :
    BehaviorSpec({
        val alertPersistencePort = mockk<AlertPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = RemoveMyAlertService(alertPersistencePort, memberUtil)

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
            When("본인의 알림을 삭제하면") {
                Then("정상적으로 삭제된다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(1L) } returns alert(1L, 10L)
                    every { alertPersistencePort.deleteById(1L) } just runs

                    val result = service.execute(1L)

                    result shouldBe true
                    verify(exactly = 1) { alertPersistencePort.deleteById(1L) }
                }
            }

            When("다른 사용자의 알림을 삭제하려 하면") {
                Then("ALERT_NOT_FOUND 예외가 발생하고 삭제는 호출되지 않는다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(2L) } returns alert(2L, 999L)

                    val exception = shouldThrow<GsmcException> { service.execute(2L) }

                    exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
                    verify(exactly = 0) { alertPersistencePort.deleteById(any()) }
                }
            }

            When("존재하지 않는 알림을 삭제하려 하면") {
                Then("ALERT_NOT_FOUND 예외가 발생한다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L) }

                    exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
                    verify(exactly = 0) { alertPersistencePort.deleteById(any()) }
                }
            }
        }
    })
