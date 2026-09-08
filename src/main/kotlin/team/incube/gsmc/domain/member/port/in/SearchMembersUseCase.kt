@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.member.port.`in`

import team.incube.gsmc.domain.member.MemberSearchResult
import team.incube.gsmc.domain.member.SearchMembersQuery

/**
 * 사용자 목록 검색 유스케이스 인터페이스입니다.
 */
interface SearchMembersUseCase {
    /**
     * 검색 조건에 맞는 사용자 목록을 조회한다.
     *
     * @param query 검색 조건
     * @return 사용자 목록
     */
    fun execute(query: SearchMembersQuery): MemberSearchResult
}
