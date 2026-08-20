package team.incube.gsmc.domain.alert.adapter.sse

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 알림 SSE 연결 관련 설정값입니다.
 *
 * @param heartbeatInterval 연결 유지를 위한 Heartbeat 전송 주기(ms)
 * @param emitterTimeout 개별 SSE 연결의 최대 유지 시간(ms)
 * @param maxConnectionsPerUser 사용자 1인당 허용하는 최대 동시 연결 수
 */
@ConfigurationProperties(prefix = "sse")
data class AlertSseProperties(
    val heartbeatInterval: Long = 30_000,
    val emitterTimeout: Long = 1_800_000,
    val maxConnectionsPerUser: Int = 5,
)
