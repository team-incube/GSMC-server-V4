package team.incube.gsmc.domain.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.global.util.MemberUtil

class RemoveMyRefreshTokenServiceTest :
    BehaviorSpec({
        val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val removeMyRefreshTokenService =
            RemoveMyRefreshTokenService(
                refreshTokenPersistencePort = refreshTokenPersistencePort,
                memberUtil = memberUtil,
            )

        beforeEach { clearAllMocks() }

        Given("인증된 사용자가 있을 때") {
            When("로그아웃하면") {
                Then("현재 사용자의 리프레시 토큰을 삭제한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { refreshTokenPersistencePort.delete(1L) } just runs

                    removeMyRefreshTokenService.execute()

                    verify(exactly = 1) { refreshTokenPersistencePort.delete(1L) }
                }
            }
        }
    })
