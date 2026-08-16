package team.incube.gsmc.domain.member.service

import team.incube.gsmc.domain.member.port.`in`.FetchMemberUseCase
import team.incube.gsmc.domain.member.port.out.MemberPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

/**
 * 사용자 단건 조회 유스케이스 구현 클래스입니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMemberService(
    private val memberPersistencePort: MemberPersistencePort,
) : FetchMemberUseCase {
    override fun execute(memberId: Long): User =
        memberPersistencePort.findByMemberId(memberId)
            ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
}
