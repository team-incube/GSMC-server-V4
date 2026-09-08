package team.incube.gsmc.domain.member.port.out

import team.incube.gsmc.domain.member.SearchMembersQuery
import team.incube.gsmc.domain.user.User

/**
 * 사용자 조회를 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface MemberPersistencePort {
    /**
     * 사용자 ID로 사용자를 조회한다.
     *
     * @param memberId 조회할 사용자ID
     * @return 해당 사용자, 없으면 null
     */
    fun findByMemberId(memberId: Long): User?

    /**
     * 검색 조건에 맞는 현재 페이지분 목록을 조회한다.
     *
     * @param query 검색 조건
     * @return 검색 조건에 분량의 페이지 목록
     */
    fun findAllBySearchCondition(query: SearchMembersQuery): List<User>

    /**
     * 검색 조건에 맞는 전체 건수를 조회한다.
     *
     * @param query 검색 조건 (페이지/정렬 관련 필드는 사용하지 않는다)
     * @return 검색 조건에 맞는 전체 건수
     */
    fun countBySearchCondition(query: SearchMembersQuery): Long
}
