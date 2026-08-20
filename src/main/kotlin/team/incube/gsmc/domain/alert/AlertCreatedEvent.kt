package team.incube.gsmc.domain.alert

/**
 * 새 알림이 저장되었음을 알리는 애플리케이션 이벤트입니다.
 *
 * [team.incube.gsmc.domain.alert.port.out.AlertEventPublisherPort]를 통해 발행되며,
 * [team.incube.gsmc.domain.alert.adapter.sse.AlertSseNotifier]가 트랜잭션 커밋 이후에만
 * 수신해 SSE로 전달한다. 순수 데이터 클래스로, Spring 이벤트 인프라에 대한 의존은 발행/구독을
 * 담당하는 adapter 계층에만 존재한다.
 *
 * @param alert 새로 저장된 알림
 */
data class AlertCreatedEvent(
    val alert: Alert,
)
