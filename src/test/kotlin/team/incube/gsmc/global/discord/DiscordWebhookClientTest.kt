package team.incube.gsmc.global.discord

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.web.client.RestClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DiscordWebhookClientTest :
    BehaviorSpec({
        Given("sendAsync") {
            When("Discord 전송이 정상적으로 이루어지면") {
                Then("RestClient로 POST 요청을 보낸다") {
                    val restClient = mockk<RestClient>(relaxed = true)
                    val client = DiscordWebhookClient(restClient)
                    val latch = CountDownLatch(1)

                    every { restClient.post() } answers {
                        latch.countDown()
                        mockk(relaxed = true)
                    }

                    client.sendAsync("https://discord.example/webhook", DiscordEmbed(title = "test", color = 0))

                    latch.await(2, TimeUnit.SECONDS)
                    verify { restClient.post() }
                }
            }

            When("RestClient 호출이 실패하면") {
                Then("예외가 호출부로 전파되지 않는다") {
                    val restClient = mockk<RestClient>()
                    every { restClient.post() } throws RuntimeException("network error")
                    val client = DiscordWebhookClient(restClient)

                    client.sendAsync("https://discord.example/webhook", DiscordEmbed(title = "test", color = 0))
                }
            }
        }
    })
