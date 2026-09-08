package team.incube.gsmc.domain.member

import team.incube.gsmc.domain.user.UserRole

/**
 * 회원 검색 조건입니다.
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
 */
data class SearchMembersQuery(
    val email: String?,
    val name: String?,
    val role: UserRole?,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
    val limit: Int,
    val page: Int,
    val sort: SortDirection,
)
