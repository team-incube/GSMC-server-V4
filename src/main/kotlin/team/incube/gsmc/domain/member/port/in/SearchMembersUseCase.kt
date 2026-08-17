@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.member.port.`in`

import team.incube.gsmc.domain.member.MemberSearchResult
import team.incube.gsmc.domain.member.SortDirection
import team.incube.gsmc.domain.user.UserRole

/**
 * 사용자 목록 검색 유스케이스 인터페이스입니다.
 */
interface SearchMembersUseCase {
    /**
     * 검색 조건에 맞는 사용자 목록을 조회한다.
     *
     * @param email 조회할 사용자 이메일
     * @param name 조회할 사용자 이름
     * @param role 조회할 사용자 권한
     * @param grade 조회할 사용자 학년
     * @param classNumber 조회할 사용자 반
     * @param number 조회할 사용자 번호
     * @param limit 페이지 크기
     * @param page 페이지 번호
     * @param sort 조회할 때 정렬 기준
     * @return 사용자 목록
     */
    fun execute(
        email: String?,
        name: String?,
        role: UserRole?,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
        limit: Int,
        page: Int,
        sort: SortDirection,
    ): MemberSearchResult
}
