package team.incube.gsmc.domain.auth.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.auth.port.out.OAuthPort
import team.incube.gsmc.domain.auth.port.out.OAuthStatePersistencePort

class FetchAuthorizationUrlServiceTest :
    BehaviorSpec({
        val oAuthPort = mockk<OAuthPort>()
        val oAuthStatePersistencePort = mockk<OAuthStatePersistencePort>()
        val fetchAuthorizationUrlService =
            FetchAuthorizationUrlService(
                oAuthPort = oAuthPort,
                oAuthStatePersistencePort = oAuthStatePersistencePort,
            )

        beforeEach { clearAllMocks() }

        Given("리다이렉트 URI가 주어졌을 때") {
            When("인가 URL을 요청하면") {
                Then("state를 저장하고 인가 URL을 반환한다") {
                    val redirectUri = "https://client.example.com/callback"
                    val codeVerifier = "code-verifier"

                    every { oAuthPort.createAuthorizationUrl(redirectUri, any()) } answers {
                        "https://auth.example.com/oauth?state=${secondArg<String>()}" to codeVerifier
                    }
                    every { oAuthStatePersistencePort.save(any(), codeVerifier) } just runs

                    val result = fetchAuthorizationUrlService.execute(redirectUri)

                    result.state.isNotBlank() shouldBe true
                    result.url shouldBe "https://auth.example.com/oauth?state=${result.state}"
                    verify(exactly = 1) { oAuthPort.createAuthorizationUrl(redirectUri, result.state) }
                    verify(exactly = 1) { oAuthStatePersistencePort.save(result.state, codeVerifier) }
                }
            }
        }
    })
