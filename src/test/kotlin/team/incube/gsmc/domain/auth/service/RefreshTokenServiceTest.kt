package team.incube.gsmc.domain.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.domain.auth.port.out.UserPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

class RefreshTokenServiceTest :
    BehaviorSpec({
        val authTokenPort = mockk<AuthTokenPort>()
        val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>()
        val userPersistencePort = mockk<UserPersistencePort>()
        val refreshTokenService =
            RefreshTokenService(
                authTokenPort = authTokenPort,
                refreshTokenPersistencePort = refreshTokenPersistencePort,
                userPersistencePort = userPersistencePort,
            )

        beforeEach { clearAllMocks() }

        fun user() =
            User(
                userId = 1,
                userName = "학생",
                userEmail = "student@gsm.hs.kr",
                userGrade = 2,
                userClassNumber = 3,
                userNumber = 4,
                userRole = UserRole.STUDENT,
            )

        Given("유효한 리프레시 토큰이 주어졌을 때") {
            When("토큰을 갱신하면") {
                Then("새 토큰을 발급하고 저장한다") {
                    val user = user()

                    every { authTokenPort.getUserIdFromToken("refresh-token") } returns user.userId
                    every { refreshTokenPersistencePort.find(user.userId) } returns "refresh-token"
                    every { userPersistencePort.findByUserId(user.userId) } returns user
                    every { authTokenPort.generateAccessToken(user.userId, user.userRole) } returns "new-access-token"
                    every { authTokenPort.generateRefreshToken(user.userId) } returns "new-refresh-token"
                    every { authTokenPort.accessTokenExpiresIn } returns 3600
                    every { authTokenPort.refreshTokenExpiresIn } returns 7200
                    every { refreshTokenPersistencePort.save(user.userId, "new-refresh-token") } just runs

                    val before = System.currentTimeMillis()
                    val result = refreshTokenService.execute("refresh-token")
                    val after = System.currentTimeMillis()

                    result.accessToken shouldBe "new-access-token"
                    result.refreshToken shouldBe "new-refresh-token"
                    result.role shouldBe UserRole.STUDENT
                    (result.accessTokenExpiresIn in (before + 3600 * 1000)..(after + 3600 * 1000)) shouldBe true
                    (result.refreshTokenExpiresIn in (before + 7200 * 1000)..(after + 7200 * 1000)) shouldBe true
                    verify(exactly = 1) { refreshTokenPersistencePort.save(user.userId, "new-refresh-token") }
                }
            }
        }

        Given("유효하지 않은 리프레시 토큰이 주어졌을 때") {
            When("토큰 파싱에 실패하면") {
                Then("INVALID_REFRESH_TOKEN 예외가 발생한다") {
                    every { authTokenPort.getUserIdFromToken("invalid-token") } throws
                        GsmcException(ErrorCode.INVALID_TOKEN)

                    val exception =
                        shouldThrow<GsmcException> {
                            refreshTokenService.execute("invalid-token")
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                    verify(exactly = 0) { refreshTokenPersistencePort.find(any()) }
                    verify(exactly = 0) { userPersistencePort.findByUserId(any()) }
                }
            }

            When("저장된 토큰이 없으면") {
                Then("INVALID_REFRESH_TOKEN 예외가 발생한다") {
                    every { authTokenPort.getUserIdFromToken("refresh-token") } returns 1L
                    every { refreshTokenPersistencePort.find(1L) } returns null

                    val exception =
                        shouldThrow<GsmcException> {
                            refreshTokenService.execute("refresh-token")
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                    verify(exactly = 0) { userPersistencePort.findByUserId(any()) }
                }
            }

            When("저장된 토큰과 다르면") {
                Then("INVALID_REFRESH_TOKEN 예외가 발생한다") {
                    every { authTokenPort.getUserIdFromToken("refresh-token") } returns 1L
                    every { refreshTokenPersistencePort.find(1L) } returns "other-refresh-token"

                    val exception =
                        shouldThrow<GsmcException> {
                            refreshTokenService.execute("refresh-token")
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_REFRESH_TOKEN
                    verify(exactly = 0) { userPersistencePort.findByUserId(any()) }
                }
            }
        }

        Given("토큰은 유효하지만 사용자가 없을 때") {
            When("토큰을 갱신하면") {
                Then("USER_NOT_FOUND 예외가 발생한다") {
                    every { authTokenPort.getUserIdFromToken("refresh-token") } returns 1L
                    every { refreshTokenPersistencePort.find(1L) } returns "refresh-token"
                    every { userPersistencePort.findByUserId(1L) } returns null

                    val exception =
                        shouldThrow<GsmcException> {
                            refreshTokenService.execute("refresh-token")
                        }

                    exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
                }
            }
        }
    })
