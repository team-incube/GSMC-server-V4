@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.alert.port.`in`

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * 내 알림 실시간 스트림(SSE) 연결 유스케이스 인터페이스입니다.
 *
 * SSE는 전송 계층 자체가 Servlet/Spring MVC에 종속된 기능이라, 이 포트는 예외적으로
 * [SseEmitter] 프레임워크 타입을 그대로 반환한다.
 */
interface ConnectMyAlertStreamUseCase {
    /**
     * @return 현재 로그인한 사용자에게 등록된 새 SSE Emitter
     */
    fun execute(): SseEmitter
}
