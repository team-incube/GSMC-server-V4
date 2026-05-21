package team.incube.gsmc.domain.auth.service

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.global.util.MemberUtil

@DisplayName("RemoveMyRefreshTokenService")
class RemoveMyRefreshTokenServiceTest {
    private val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>()
    private val memberUtil = mockk<MemberUtil>()
    private val removeMyRefreshTokenService =
        RemoveMyRefreshTokenService(
            refreshTokenPersistencePort = refreshTokenPersistencePort,
            memberUtil = memberUtil,
        )

    @Nested
    @DisplayName("Given 인증된 사용자가 있을 때")
    inner class GivenAuthenticatedUser {
        @Test
        @DisplayName("When 로그아웃하면 Then 현재 사용자의 리프레시 토큰을 삭제한다")
        fun `로그아웃하면 현재 사용자의 리프레시 토큰을 삭제한다`() {
            every { memberUtil.getCurrentUserId() } returns 1L
            every { refreshTokenPersistencePort.delete(1L) } just runs

            removeMyRefreshTokenService.execute()

            verify(exactly = 1) { refreshTokenPersistencePort.delete(1L) }
        }
    }
}
