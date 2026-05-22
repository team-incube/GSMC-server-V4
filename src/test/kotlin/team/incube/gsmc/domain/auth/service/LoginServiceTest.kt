package team.incube.gsmc.domain.auth.service

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.SimpleTransactionStatus
import team.incube.gsmc.domain.auth.OAuthTokenResult
import team.incube.gsmc.domain.auth.OAuthUserInfo
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.auth.port.out.OAuthPort
import team.incube.gsmc.domain.auth.port.out.OAuthStatePersistencePort
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.domain.auth.port.out.UserPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

@DisplayName("LoginService")
class LoginServiceTest {
    private val oAuthPort = mockk<OAuthPort>()
    private val oAuthStatePersistencePort = mockk<OAuthStatePersistencePort>()
    private val userPersistencePort = mockk<UserPersistencePort>()
    private val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>()
    private val authTokenPort = mockk<AuthTokenPort>()
    private val transactionManager = mockk<PlatformTransactionManager>()
    private val loginService =
        LoginService(
            oAuthPort = oAuthPort,
            oAuthStatePersistencePort = oAuthStatePersistencePort,
            userPersistencePort = userPersistencePort,
            refreshTokenPersistencePort = refreshTokenPersistencePort,
            authTokenPort = authTokenPort,
            transactionManager = transactionManager,
        )

    @Nested
    @DisplayName("Given 유효한 OAuth state가 주어졌을 때")
    inner class GivenValidOAuthState {
        @Test
        @DisplayName("When 기존 사용자가 로그인하면 Then 토큰을 발급하고 리프레시 토큰을 저장한다")
        fun `기존 사용자가 로그인하면 토큰을 발급하고 리프레시 토큰을 저장한다`() {
            val user = studentUser()

            everySuccessfulTransaction()
            everyOAuthLogin(oAuthUserInfo = studentOAuthUserInfo())
            every { userPersistencePort.findByEmail(user.userEmail) } returns user
            every { authTokenPort.generateAccessToken(user.userId, user.userRole) } returns "access-token"
            every { authTokenPort.generateRefreshToken(user.userId) } returns "refresh-token"
            every { authTokenPort.accessTokenExpiresIn } returns 3600
            every { authTokenPort.refreshTokenExpiresIn } returns 7200
            every { refreshTokenPersistencePort.save(user.userId, "refresh-token") } just runs

            val before = System.currentTimeMillis()
            val result =
                loginService.execute(
                    code = "code",
                    state = "state",
                    redirectUri = "https://client.example.com/callback",
                )
            val after = System.currentTimeMillis()

            assertEquals("access-token", result.accessToken)
            assertEquals("refresh-token", result.refreshToken)
            assertEquals(UserRole.STUDENT, result.role)
            assertTrue(result.accessTokenExpiresIn in (before + 3600 * 1000)..(after + 3600 * 1000))
            assertTrue(result.refreshTokenExpiresIn in (before + 7200 * 1000)..(after + 7200 * 1000))
            verify(exactly = 0) { userPersistencePort.save(any()) }
            verify(exactly = 1) { refreshTokenPersistencePort.save(user.userId, "refresh-token") }
        }

        @Test
        @DisplayName("When 신규 학생이 로그인하면 Then 학생 사용자로 저장한 뒤 토큰을 발급한다")
        fun `신규 학생이 로그인하면 학생 사용자로 저장한 뒤 토큰을 발급한다`() {
            val savedUser = studentUser()
            val userSlot = slot<User>()

            everySuccessfulTransaction()
            everyOAuthLogin(oAuthUserInfo = studentOAuthUserInfo())
            every { userPersistencePort.findByEmail("student@gsm.hs.kr") } returns null
            every { userPersistencePort.save(capture(userSlot)) } returns savedUser
            every { authTokenPort.generateAccessToken(savedUser.userId, savedUser.userRole) } returns "access-token"
            every { authTokenPort.generateRefreshToken(savedUser.userId) } returns "refresh-token"
            every { authTokenPort.accessTokenExpiresIn } returns 3600
            every { authTokenPort.refreshTokenExpiresIn } returns 7200
            every { refreshTokenPersistencePort.save(savedUser.userId, "refresh-token") } just runs

            val result =
                loginService.execute(
                    code = "code",
                    state = "state",
                    redirectUri = "https://client.example.com/callback",
                )

            assertEquals(UserRole.STUDENT, result.role)
            assertEquals("학생", userSlot.captured.userName)
            assertEquals("student@gsm.hs.kr", userSlot.captured.userEmail)
            assertEquals(2, userSlot.captured.userGrade)
            assertEquals(3, userSlot.captured.userClassNumber)
            assertEquals(4, userSlot.captured.userNumber)
            assertEquals(UserRole.STUDENT, userSlot.captured.userRole)
        }

        @Test
        @DisplayName("When 신규 교사가 로그인하면 Then 이메일을 이름으로 사용해 교사 사용자로 저장한다")
        fun `신규 교사가 로그인하면 이메일을 이름으로 사용해 교사 사용자로 저장한다`() {
            val teacherEmail = "teacher@gsm.hs.kr"
            val savedUser =
                User(
                    userId = 2,
                    userName = teacherEmail,
                    userEmail = teacherEmail,
                    userGrade = null,
                    userClassNumber = null,
                    userNumber = null,
                    userRole = UserRole.TEACHER,
                )
            val userSlot = slot<User>()

            everySuccessfulTransaction()
            everyOAuthLogin(
                oAuthUserInfo =
                    OAuthUserInfo(
                        email = teacherEmail,
                        isStudent = false,
                        name = null,
                        grade = null,
                        classNum = null,
                        number = null,
                    ),
            )
            every { userPersistencePort.findByEmail(teacherEmail) } returns null
            every { userPersistencePort.save(capture(userSlot)) } returns savedUser
            every { authTokenPort.generateAccessToken(savedUser.userId, savedUser.userRole) } returns "access-token"
            every { authTokenPort.generateRefreshToken(savedUser.userId) } returns "refresh-token"
            every { authTokenPort.accessTokenExpiresIn } returns 3600
            every { authTokenPort.refreshTokenExpiresIn } returns 7200
            every { refreshTokenPersistencePort.save(savedUser.userId, "refresh-token") } just runs

            val result =
                loginService.execute(
                    code = "code",
                    state = "state",
                    redirectUri = "https://client.example.com/callback",
                )

            assertEquals(UserRole.TEACHER, result.role)
            assertEquals(teacherEmail, userSlot.captured.userName)
            assertEquals(UserRole.TEACHER, userSlot.captured.userRole)
            assertEquals(null, userSlot.captured.userGrade)
            assertEquals(null, userSlot.captured.userClassNumber)
            assertEquals(null, userSlot.captured.userNumber)
        }
    }

    @Nested
    @DisplayName("Given OAuth state가 유효하지 않을 때")
    inner class GivenInvalidOAuthState {
        @Test
        @DisplayName("When 로그인을 요청하면 Then INVALID_OAUTH_STATE 예외가 발생한다")
        fun `로그인을 요청하면 INVALID_OAUTH_STATE 예외가 발생한다`() {
            every { oAuthStatePersistencePort.findAndDelete("invalid-state") } returns null

            val exception =
                assertThrows(GsmcException::class.java) {
                    loginService.execute(
                        code = "code",
                        state = "invalid-state",
                        redirectUri = "https://client.example.com/callback",
                    )
                }

            assertEquals(ErrorCode.INVALID_OAUTH_STATE, exception.errorCode)
            verify(exactly = 0) { oAuthPort.exchangeCodeForToken(any(), any(), any()) }
        }
    }

    private fun everySuccessfulTransaction() {
        every { transactionManager.getTransaction(any<TransactionDefinition>()) } returns SimpleTransactionStatus()
        every { transactionManager.commit(any()) } just runs
        every { transactionManager.rollback(any()) } just runs
    }

    private fun everyOAuthLogin(oAuthUserInfo: OAuthUserInfo) {
        every { oAuthStatePersistencePort.findAndDelete("state") } returns "code-verifier"
        every {
            oAuthPort.exchangeCodeForToken(
                code = "code",
                redirectUri = "https://client.example.com/callback",
                codeVerifier = "code-verifier",
            )
        } returns OAuthTokenResult(accessToken = "oauth-access-token", refreshToken = null, expiresIn = 3600)
        every { oAuthPort.getUserInfo("oauth-access-token") } returns oAuthUserInfo
    }

    private fun studentOAuthUserInfo(): OAuthUserInfo =
        OAuthUserInfo(
            email = "student@gsm.hs.kr",
            isStudent = true,
            name = "학생",
            grade = 2,
            classNum = 3,
            number = 4,
        )

    private fun studentUser(): User =
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
