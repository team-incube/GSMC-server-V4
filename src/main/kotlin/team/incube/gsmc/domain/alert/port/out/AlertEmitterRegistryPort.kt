package team.incube.gsmc.domain.alert.port.out

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * 사용자별 SSE 연결(Emitter)을 관리하는 아웃바운드 포트 인터페이스입니다.
 *
 * SSE는 전송 계층 자체가 Servlet/Spring MVC에 종속된 기능이라, 이 포트는 예외적으로
 * [SseEmitter] 프레임워크 타입을 그대로 노출한다. 순수 도메인 모델로 추상화하기보다, 얇은 경계로
 * 취급하는 편이 불필요한 복잡도를 피할 수 있다고 판단했다.
 */
interface AlertEmitterRegistryPort {
    /**
     * 현재 사용자를 위한 새 SSE 연결을 생성하고 등록한다. 연결 종료/타임아웃/오류 시 자동으로
     * 자신을 해제하도록 콜백이 함께 구성된다. 사용자별 최대 연결 수를 초과하면 가장 오래된 연결을
     * 정리한 뒤 새 연결을 등록한다.
     *
     * @param userId 연결을 등록할 사용자 ID
     * @return 새로 생성되고 등록된 Emitter
     */
    fun createAndRegister(userId: Long): SseEmitter

    /**
     * 등록된 연결을 제거한다. 이미 제거된 연결에 대해 호출해도 안전하다.
     *
     * @param userId 연결이 등록된 사용자 ID
     * @param emitter 제거할 Emitter
     */
    fun remove(
        userId: Long,
        emitter: SseEmitter,
    )

    /**
     * 특정 사용자의 모든 활성 연결을 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 활성 Emitter 목록, 없으면 빈 목록
     */
    fun findAllByUserId(userId: Long): List<SseEmitter>
}
