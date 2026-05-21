package team.incube.gsmc.domain.auth.service

import team.incube.gsmc.domain.auth.port.`in`.LogoutUseCase
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port

/**
 * 로그아웃 유스케이스 구현 클래스입니다.
 * [LogoutUseCase]를 구현하며, [RefreshTokenPersistencePort]를 통해 Redis에 저장된 리프레시 토큰을 삭제하여 로그아웃 처리합니다.
 */
@Port(direction = PortDirection.INBOUND)
class LogoutService(
    private val refreshTokenPersistencePort: RefreshTokenPersistencePort,
) : LogoutUseCase {
    /**
     * @param userId 로그아웃할 사용자 ID
     */
    override fun execute(userId: Long) {
        refreshTokenPersistencePort.delete(userId)
    }
}
