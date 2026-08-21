package team.incube.gsmc.domain.alert.port.out

import team.incube.gsmc.domain.alert.Alert

/**
 * 새 알림 생성을 실시간 채널(SSE)로 전달하기 위해 이벤트를 발행하는 아웃바운드 포트 인터페이스입니다.
 * 알림을 생성하는 Service는 이 포트만 알고, 실제 전송 방식(Spring Event, SSE 등)은 모른다.
 */
interface AlertEventPublisherPort {
    /**
     * 알림이 생성되었음을 발행한다. 실제 전달은 호출 시점의 트랜잭션이 커밋된 이후에만 이뤄진다.
     *
     * @param alert 새로 저장된 알림
     */
    fun publish(alert: Alert)
}
