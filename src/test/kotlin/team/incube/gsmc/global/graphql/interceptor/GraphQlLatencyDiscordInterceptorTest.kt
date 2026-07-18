package team.incube.gsmc.global.graphql.interceptor

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.graphql.ResponseError
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import reactor.core.publisher.Mono
import team.incube.gsmc.global.discord.DiscordEmbed
import team.incube.gsmc.global.discord.DiscordWebhookClient

class GraphQlLatencyDiscordInterceptorTest :
    BehaviorSpec({
        fun request(
            document: String = "query { me { name } }",
            variables: Map<String, Any> = emptyMap(),
        ): WebGraphQlRequest {
            val req = mockk<WebGraphQlRequest>()
            every { req.document } returns document
            every { req.variables } returns variables
            return req
        }

        fun response(errors: List<ResponseError> = emptyList()): WebGraphQlResponse {
            val res = mockk<WebGraphQlResponse>()
            every { res.errors } returns errors
            return res
        }

        fun chainReturning(res: WebGraphQlResponse): WebGraphQlInterceptor.Chain =
            WebGraphQlInterceptor.Chain { _ -> Mono.just(res) }

        Given("intercept") {
            When("에러 없이 응답이 오면") {
                Then("GraphQL 요청 정보가 담긴 embed가 DiscordWebhookClient로 전달된다") {
                    val discordWebhookClient = mockk<DiscordWebhookClient>(relaxed = true)
                    val interceptor =
                        GraphQlLatencyDiscordInterceptor(discordWebhookClient, "https://discord.example/webhook")
                    val embedSlot = slot<DiscordEmbed>()

                    interceptor.intercept(request(), chainReturning(response())).block()

                    verify { discordWebhookClient.sendAsync(any(), capture(embedSlot)) }
                    embedSlot.captured.title shouldBe "GraphQL 요청"
                    embedSlot.captured.color shouldBe DiscordEmbed.COLOR_GREEN
                }
            }

            When("GraphQL 에러가 응답에 포함되면") {
                Then("응답속도와 무관하게 빨간색으로 표시된다") {
                    val discordWebhookClient = mockk<DiscordWebhookClient>(relaxed = true)
                    val interceptor =
                        GraphQlLatencyDiscordInterceptor(discordWebhookClient, "https://discord.example/webhook")
                    val error = mockk<ResponseError>()
                    every { error.message } returns "실패"
                    val embedSlot = slot<DiscordEmbed>()

                    interceptor.intercept(request(), chainReturning(response(listOf(error)))).block()

                    verify { discordWebhookClient.sendAsync(any(), capture(embedSlot)) }
                    embedSlot.captured.color shouldBe DiscordEmbed.COLOR_RED
                    embedSlot.captured.fields
                        .first { it.name == "에러" }
                        .value shouldContain "실패"
                }
            }

            When("변수에 password 키가 포함되면") {
                Then("값이 마스킹되어 전달된다") {
                    val discordWebhookClient = mockk<DiscordWebhookClient>(relaxed = true)
                    val interceptor =
                        GraphQlLatencyDiscordInterceptor(discordWebhookClient, "https://discord.example/webhook")
                    val embedSlot = slot<DiscordEmbed>()
                    val req = request(variables = mapOf("password" to "hunter2", "categoryType" to "TOEIC"))

                    interceptor.intercept(req, chainReturning(response())).block()

                    verify { discordWebhookClient.sendAsync(any(), capture(embedSlot)) }
                    val variablesField = embedSlot.captured.fields.first { it.name == "변수" }
                    variablesField.value shouldContain "***"
                    variablesField.value shouldNotContain "hunter2"
                }
            }

            When("변수가 input 객체로 중첩되어 password 키를 포함하면") {
                Then("중첩된 값까지 재귀적으로 마스킹되어 전달된다") {
                    val discordWebhookClient = mockk<DiscordWebhookClient>(relaxed = true)
                    val interceptor =
                        GraphQlLatencyDiscordInterceptor(discordWebhookClient, "https://discord.example/webhook")
                    val embedSlot = slot<DiscordEmbed>()
                    val req =
                        request(
                            variables =
                                mapOf(
                                    "categoryType" to "TOEIC",
                                    "input" to mapOf("password" to "hunter2", "value" to "8"),
                                ),
                        )

                    interceptor.intercept(req, chainReturning(response())).block()

                    verify { discordWebhookClient.sendAsync(any(), capture(embedSlot)) }
                    val variablesField = embedSlot.captured.fields.first { it.name == "변수" }
                    variablesField.value shouldContain "***"
                    variablesField.value shouldNotContain "hunter2"
                }
            }

            When("쿼리 원문이 900자를 넘으면") {
                Then("900자로 잘리고 말줄임표가 붙는다") {
                    val discordWebhookClient = mockk<DiscordWebhookClient>(relaxed = true)
                    val interceptor =
                        GraphQlLatencyDiscordInterceptor(discordWebhookClient, "https://discord.example/webhook")
                    val longQuery = "a".repeat(1000)
                    val embedSlot = slot<DiscordEmbed>()

                    interceptor.intercept(request(document = longQuery), chainReturning(response())).block()

                    verify { discordWebhookClient.sendAsync(any(), capture(embedSlot)) }
                    val queryField = embedSlot.captured.fields.first { it.name == "쿼리" }
                    queryField.value shouldBe "a".repeat(900) + "..."
                }
            }
        }
    })
