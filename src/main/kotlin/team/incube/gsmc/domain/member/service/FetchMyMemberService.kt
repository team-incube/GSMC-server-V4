package team.incube.gsmc.domain.member.service

import team.incube.gsmc.domain.member.port.`in`.FetchMyMemberUseCase
import team.incube.gsmc.domain.member.port.out.MemberPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 현재 로그인 사용자 조회 유스케이스 구현 클래스입니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyMemberService(
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyMemberUseCase {
    override fun execute(): User {
        val memberId = memberUtil.getCurrentUserId()
        return memberPersistencePort.findByMemberId(memberId)
            ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
    }
}
