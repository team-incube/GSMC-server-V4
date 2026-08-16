package team.incube.gsmc.domain.member.service

import team.incube.gsmc.domain.member.MemberSearchResult
import team.incube.gsmc.domain.member.SortDirection
import team.incube.gsmc.domain.member.port.`in`.SearchMembersUseCase
import team.incube.gsmc.domain.member.port.out.MemberPersistencePort
import team.incube.gsmc.domain.user.UserRole
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
    override fun execute(
        email: String?,
        name: String?,
        role: UserRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        limit: Int,
        page: Int,
        sortDirection: SortDirection,
    ): MemberSearchResult {
        if (limit <= 0) throw GsmcException(ErrorCode.INVALID_PAGE_SIZE)

        val members =
            memberPersistencePort.findAllBySearchCondition(
                email,
                name,
                role,
                grade,
                classNumber,
                number,
                limit,
                page,
                sortDirection,
            )
        val totalElements = memberPersistencePort.countBySearchCondition(email, name, role, grade, classNumber, number)
        val totalPages = ((totalElements + limit - 1) / limit).toInt()
        return MemberSearchResult(members, totalElements, totalPages)
    }
}
