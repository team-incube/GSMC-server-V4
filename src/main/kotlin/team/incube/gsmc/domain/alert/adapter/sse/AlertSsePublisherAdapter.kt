package team.incube.gsmc.domain.alert.adapter.sse

import org.springframework.context.ApplicationEventPublisher
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.AlertCreatedEvent
import team.incube.gsmc.domain.alert.port.out.AlertEventPublisherPort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * [AlertEventPublisherPort]를 Spring의 [ApplicationEventPublisher]로 구현하는 아웃바운드 어댑터입니다.
 * 실제 SSE 전송은 하지 않고 [AlertCreatedEvent]를 발행만 하며, 트랜잭션 커밋 이후 전달을 보장하는
 * 책임은 이 이벤트를 구독하는 [AlertSseNotifier]의 `@TransactionalEventListener`가 담당한다.
 * Kafka·Redis Pub/Sub 같은 별도 메시지 브로커 없이 Spring이 기본 제공하는 이벤트 인프라만으로
 * 요구사항을 충족한다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class AlertSsePublisherAdapter(
    private val eventPublisher: ApplicationEventPublisher,
) : AlertEventPublisherPort {
    override fun publish(alert: Alert) {
        eventPublisher.publishEvent(AlertCreatedEvent(alert))
    }
}
