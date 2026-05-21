package team.incube.gsmc.domain.auth.service

import team.incube.gsmc.domain.auth.port.`in`.LogoutUseCase
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

@Port(direction = PortDirection.INBOUND)
class RemoveMyRefreshTokenService(
    private val refreshTokenPersistencePort: RefreshTokenPersistencePort,
    private val memberUtil: MemberUtil,
) : LogoutUseCase {
    override fun execute() {
        refreshTokenPersistencePort.delete(memberUtil.getCurrentUserId())
    }
}
