package team.incube.gsmc.domain.alert.adapter.web

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import team.incube.gsmc.domain.alert.port.`in`.ConnectMyAlertStreamUseCase

private const val NO_CACHE = "no-cache"
private const val KEEP_ALIVE = "keep-alive"

/**
 * 알림 실시간 스트림(SSE) 연결 REST 엔드포인트입니다.
 * GraphQL Query/Mutation([AlertWebAdapter])과 별도로, 기존 JWT 인증·인가 체계를 그대로 통과하는
 * REST 엔드포인트로 제공합니다. 연결 등록과 이벤트 전송 로직은 전혀 갖지 않고 UseCase에 위임합니다.
 * Nginx 등 Reverse Proxy가 응답을 버퍼링해 이벤트 전달이 지연되지 않도록 `X-Accel-Buffering: no`를
 * 함께 내려준다.
 */
@RestController
@RequestMapping("/api/alerts")
class AlertSseWebAdapter(
    private val connectMyAlertStreamUseCase: ConnectMyAlertStreamUseCase,
) {
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): ResponseEntity<SseEmitter> =
        ResponseEntity
            .ok()
            .header(HttpHeaders.CACHE_CONTROL, NO_CACHE)
            .header(HttpHeaders.CONNECTION, KEEP_ALIVE)
            .header("X-Accel-Buffering", "no")
            .body(connectMyAlertStreamUseCase.execute())
}
