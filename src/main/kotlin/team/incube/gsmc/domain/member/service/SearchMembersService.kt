package team.incube.gsmc.domain.member.service

import team.incube.gsmc.domain.member.MemberSearchResult
import team.incube.gsmc.domain.member.SearchMembersQuery
import team.incube.gsmc.domain.member.port.`in`.SearchMembersUseCase
import team.incube.gsmc.domain.member.port.out.MemberPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

/**
 * 회원 목록 검색 유스케이스 구현 클래스입니다.
 */
@Port(direction = PortDirection.INBOUND)
class SearchMembersService(
    private val memberPersistencePort: MemberPersistencePort,
) : SearchMembersUseCase {
    override fun execute(query: SearchMembersQuery): MemberSearchResult {
        if (query.limit <= 0) throw GsmcException(ErrorCode.INVALID_PAGE_SIZE)

        val members = memberPersistencePort.findAllBySearchCondition(query)
        val totalElements = memberPersistencePort.countBySearchCondition(query)
        val totalPages = ((totalElements + query.limit - 1) / query.limit).toInt()
        return MemberSearchResult(members, totalElements, totalPages)
    }
}
