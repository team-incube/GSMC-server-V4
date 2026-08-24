package team.incube.gsmc.domain.alert.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.gsmc.domain.alert.port.out.AlertEmitterRegistryPort
import team.incube.gsmc.global.util.MemberUtil
import java.io.IOException

class ConnectMyAlertStreamServiceTest :
    BehaviorSpec({
        val alertEmitterRegistryPort = mockk<AlertEmitterRegistryPort>()
        val memberUtil = mockk<MemberUtil>()
        val service = ConnectMyAlertStreamService(alertEmitterRegistryPort, memberUtil)

        beforeEach { clearAllMocks() }

        Given("로그인한 사용자가") {
            When("SSE 스트림에 연결하면") {
                Then("Registry에 연결을 등록하고 connected 이벤트를 전송한다") {
                    val emitter = mockk<SseEmitter>()
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertEmitterRegistryPort.createAndRegister(10L) } returns emitter
                    every { emitter.send(any<SseEmitter.SseEventBuilder>()) } just runs

                    val result = service.execute()

                    result shouldBe emitter
                    verify(exactly = 1) { alertEmitterRegistryPort.createAndRegister(10L) }
                    verify(exactly = 1) { emitter.send(any<SseEmitter.SseEventBuilder>()) }
                    verify(exactly = 0) { alertEmitterRegistryPort.remove(any(), any()) }
                }
            }

            When("connected 이벤트 전송이 실패하면") {
                Then("등록했던 연결을 정리한다") {
                    val emitter = mockk<SseEmitter>()
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertEmitterRegistryPort.createAndRegister(10L) } returns emitter
                    every { emitter.send(any<SseEmitter.SseEventBuilder>()) } throws IOException("closed")
                    every { alertEmitterRegistryPort.remove(10L, emitter) } just runs

                    service.execute()

                    verify(exactly = 1) { alertEmitterRegistryPort.remove(10L, emitter) }
                }
            }
        }
    })
