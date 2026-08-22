package team.incube.gsmc.domain.alert.adapter.sse

import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.AlertType
import java.time.LocalDateTime

/**
 * SSE `alert` 이벤트로 전송되는 페이로드입니다. GraphQL `Alert` 타입과 마찬가지로 수신자 본인에게만
 * 전달되는 채널이라 `userId`는 포함하지 않는다.
 */
data class AlertEventPayload(
    val alertId: Long,
    val alertType: AlertType,
    val scoreId: Long?,
    val content: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(alert: Alert): AlertEventPayload =
            AlertEventPayload(
                alertId = alert.alertId,
                alertType = alert.alertType,
                scoreId = alert.scoreId,
                content = alert.content,
                isRead = alert.isRead,
                createdAt = alert.createdAt,
            )
    }
}
