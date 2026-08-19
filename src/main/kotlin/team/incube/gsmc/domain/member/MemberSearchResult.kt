package team.incube.gsmc.domain.member
import team.incube.gsmc.domain.user.User

/**
 * 회원 검색 결과
 *
 * @param members 검색된 회원 목록
 * @param totalElements 전체 검색 결과 수
 * @param totalPages 전체 페이지 수
 */
data class MemberSearchResult(
    val members: List<User>,
    val totalElements: Long,
    val totalPages: Int,
)
