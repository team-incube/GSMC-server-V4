package team.incube.gsmc.global.graphql.interceptor

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import team.incube.gsmc.global.auth.CustomUserDetails
import team.incube.gsmc.global.discord.DiscordEmbed
import team.incube.gsmc.global.discord.DiscordWebhookClient

@Component
class GraphQlLatencyDiscordInterceptor(
    private val discordWebhookClient: DiscordWebhookClient,
    @param:Value("\${discord.webhook.graphql-latency-url}") private val webhookUrl: String,
) : WebGraphQlInterceptor {
    private val logger = LoggerFactory.getLogger(GraphQlLatencyDiscordInterceptor::class.java)

    override fun intercept(
        request: WebGraphQlRequest,
        chain: WebGraphQlInterceptor.Chain,
    ): Mono<WebGraphQlResponse> {
        val start = System.currentTimeMillis()
        // GraphQL 실행은 다른 스레드로 넘어갈 수 있어 doOnNext 안에서 SecurityContextHolder를 읽으면
        // ThreadLocal이 비어 요청자 정보를 잃을 수 있다. 원 요청 스레드에서 동기적으로 미리 캡처한다.
        val authentication = SecurityContextHolder.getContext().authentication
        return chain
            .next(request)
            .doOnNext { response ->
                runCatching { report(request, response, System.currentTimeMillis() - start, authentication) }
                    .onFailure { logger.warn("GraphQL 응답속도 Discord 알림 실패: {}", it.message) }
            }
    }

    private fun report(
        request: WebGraphQlRequest,
        response: WebGraphQlResponse,
        elapsedMs: Long,
        authentication: Authentication?,
    ) {
        val hasErrors = response.errors.isNotEmpty()
        val color =
            when {
                hasErrors -> DiscordEmbed.COLOR_RED
                elapsedMs < 300 -> DiscordEmbed.COLOR_GREEN
                elapsedMs < 700 -> DiscordEmbed.COLOR_YELLOW
                elapsedMs < 1000 -> DiscordEmbed.COLOR_ORANGE
                else -> DiscordEmbed.COLOR_RED
            }

        val fields =
            buildList {
                add(DiscordEmbed.Field("응답시간", "${elapsedMs}ms", inline = true))
                add(DiscordEmbed.Field("요청자", requesterInfo(authentication), inline = true))
                add(DiscordEmbed.Field("쿼리", truncate(request.document), inline = false))
                if (request.variables.isNotEmpty()) {
                    add(DiscordEmbed.Field("변수", truncate(maskSensitive(request.variables).toString()), inline = false))
                }
                if (hasErrors) {
                    add(
                        DiscordEmbed.Field(
                            "에러",
                            truncate(response.errors.joinToString { it.message.orEmpty() }),
                            inline = false,
                        ),
                    )
                }
            }

        discordWebhookClient.sendAsync(
            webhookUrl,
            DiscordEmbed(title = "GraphQL 요청", color = color, fields = fields),
        )
    }

    private fun requesterInfo(authentication: Authentication?): String {
        val principal = authentication?.principal
        return if (principal is CustomUserDetails) "${principal.userId} (${principal.userRole})" else "익명"
    }

    @Suppress("UNCHECKED_CAST")
    private fun maskSensitive(variables: Map<String, Any?>): Map<String, Any?> =
        maskValue(variables) as Map<String, Any?>

    private fun maskValue(value: Any?): Any? =
        when (value) {
            is Map<*, *> ->
                value.entries.associate { (key, nested) ->
                    val keyName = key.toString()
                    keyName to if (SENSITIVE_KEY_PATTERN.containsMatchIn(keyName)) "***" else maskValue(nested)
                }
            is List<*> -> value.map { maskValue(it) }
            else -> value
        }

    private fun truncate(text: String): String = if (text.length > 900) text.take(900) + "..." else text

    companion object {
        private val SENSITIVE_KEY_PATTERN = Regex("password|token|secret", RegexOption.IGNORE_CASE)
    }
}
