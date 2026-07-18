package team.incube.gsmc.global.discord

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class DiscordWebhookClient(
    private val restClient: RestClient,
) {
    private val logger = LoggerFactory.getLogger(DiscordWebhookClient::class.java)

    fun sendAsync(
        webhookUrl: String,
        embed: DiscordEmbed,
    ) {
        if (webhookUrl.isBlank()) return
        Mono
            .fromRunnable<Unit> { send(webhookUrl, embed) }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
    }

    private fun send(
        webhookUrl: String,
        embed: DiscordEmbed,
    ) {
        runCatching {
            restClient
                .post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("embeds" to listOf(embed.toPayload())))
                .retrieve()
                .toBodilessEntity()
        }.onFailure { logger.warn("Discord webhook 전송 실패: {}", it.message) }
    }

    private fun DiscordEmbed.toPayload(): Map<String, Any?> =
        mapOf(
            "title" to title,
            "description" to description,
            "color" to color,
            "fields" to
                fields.map {
                    mapOf("name" to it.name, "value" to it.value, "inline" to it.inline)
                },
        )
}
