package team.incube.gsmc.domain.member.adapter.web

import team.incube.gsmc.domain.member.MemberSearchResult

/**
 * 회원 검색 Query의 GraphQL 응답 DTO입니다.
 * 도메인 [MemberSearchResult]의 `members`(`List<User>`)를 [MemberPayload] 목록으로,
 * `totalElements`(`Long`)를 `Int`로 변환합니다.
 */
data class SearchMemberPayload(
    val members: List<MemberPayload>,
    val totalPages: Int,
    val totalElements: Int,
)

/**
 * 도메인 모델 [MemberSearchResult]를 [SearchMemberPayload]로 변환한다.
 *
 * @receiver 변환할 도메인 객체
 * @return 변환된 [SearchMemberPayload]
 */
fun MemberSearchResult.toPayload(): SearchMemberPayload =
    SearchMemberPayload(
        members = members.map { it.toPayload() },
        totalPages = totalPages,
        totalElements = totalElements.toInt(),
    )
