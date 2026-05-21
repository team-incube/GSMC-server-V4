package team.incube.gsmc.domain.auth.service

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.domain.auth.port.out.UserPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {
    private val authTokenPort = mockk<AuthTokenPort>()
    private val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>()
    private val userPersistencePort = mockk<UserPersistencePort>()
    private val refreshTokenService =
        RefreshTokenService(
            authTokenPort = authTokenPort,
            refreshTokenPersistencePort = refreshTokenPersistencePort,
            userPersistencePort = userPersistencePort,
        )

    @Nested
    @DisplayName("Given 유효한 리프레시 토큰이 주어졌을 때")
    inner class GivenValidRefreshToken {
        @Test
        @DisplayName("When 토큰을 갱신하면 Then 새 토큰을 발급하고 저장한다")
        fun `토큰을 갱신하면 새 토큰을 발급하고 저장한다`() {
            val user = user()

            every { authTokenPort.parseTokenClaims("refresh-token") } returns (user.userId to user.userRole)
            every { refreshTokenPersistencePort.find(user.userId) } returns "refresh-token"
            every { userPersistencePort.findByUserId(user.userId) } returns user
            every { authTokenPort.generateAccessToken(user.userId, user.userRole) } returns "new-access-token"
            every { authTokenPort.generateRefreshToken(user.userId) } returns "new-refresh-token"
            every { authTokenPort.accessTokenExpiresIn } returns 3600
            every { authTokenPort.refreshTokenExpiresIn } returns 7200
            every { refreshTokenPersistencePort.save(user.userId, "new-refresh-token") } just runs

            val result = refreshTokenService.execute("refresh-token")

            assertEquals("new-access-token", result.accessToken)
            assertEquals("new-refresh-token", result.refreshToken)
            assertEquals(UserRole.STUDENT, result.role)
            assertTrue(result.accessTokenExpiresIn > System.currentTimeMillis())
            assertTrue(result.refreshTokenExpiresIn > result.accessTokenExpiresIn)
            verify(exactly = 1) { refreshTokenPersistencePort.save(user.userId, "new-refresh-token") }
        }
    }

    @Nested
    @DisplayName("Given 유효하지 않은 리프레시 토큰이 주어졌을 때")
    inner class GivenInvalidRefreshToken {
        @Test
        @DisplayName("When 토큰 파싱에 실패하면 Then INVALID_REFRESH_TOKEN 예외가 발생한다")
        fun `토큰 파싱에 실패하면 INVALID_REFRESH_TOKEN 예외가 발생한다`() {
            every { authTokenPort.parseTokenClaims("invalid-token") } returns null

            val exception =
                assertThrows(GsmcException::class.java) {
                    refreshTokenService.execute("invalid-token")
                }

            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
            verify(exactly = 0) { refreshTokenPersistencePort.find(any()) }
        }

        @Test
        @DisplayName("When 저장된 토큰이 없으면 Then INVALID_REFRESH_TOKEN 예외가 발생한다")
        fun `저장된 토큰이 없으면 INVALID_REFRESH_TOKEN 예외가 발생한다`() {
            every { authTokenPort.parseTokenClaims("refresh-token") } returns (1L to UserRole.STUDENT)
            every { refreshTokenPersistencePort.find(1L) } returns null

            val exception =
                assertThrows(GsmcException::class.java) {
                    refreshTokenService.execute("refresh-token")
                }

            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
        }

        @Test
        @DisplayName("When 저장된 토큰과 다르면 Then INVALID_REFRESH_TOKEN 예외가 발생한다")
        fun `저장된 토큰과 다르면 INVALID_REFRESH_TOKEN 예외가 발생한다`() {
            every { authTokenPort.parseTokenClaims("refresh-token") } returns (1L to UserRole.STUDENT)
            every { refreshTokenPersistencePort.find(1L) } returns "other-refresh-token"

            val exception =
                assertThrows(GsmcException::class.java) {
                    refreshTokenService.execute("refresh-token")
                }

            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("Given 토큰은 유효하지만 사용자가 없을 때")
    inner class GivenMissingUser {
        @Test
        @DisplayName("When 토큰을 갱신하면 Then USER_NOT_FOUND 예외가 발생한다")
        fun `토큰을 갱신하면 USER_NOT_FOUND 예외가 발생한다`() {
            every { authTokenPort.parseTokenClaims("refresh-token") } returns (1L to UserRole.STUDENT)
            every { refreshTokenPersistencePort.find(1L) } returns "refresh-token"
            every { userPersistencePort.findByUserId(1L) } returns null

            val exception =
                assertThrows(GsmcException::class.java) {
                    refreshTokenService.execute("refresh-token")
                }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }
    }

    private fun user(): User =
        User(
            userId = 1,
            userName = "학생",
            userEmail = "student@gsm.hs.kr",
            userGrade = 2,
            userClassNumber = 3,
            userNumber = 4,
            userRole = UserRole.STUDENT,
        )
}
