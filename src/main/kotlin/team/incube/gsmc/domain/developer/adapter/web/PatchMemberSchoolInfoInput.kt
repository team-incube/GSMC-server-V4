package team.incube.gsmc.domain.developer.adapter.web

/**
 * 회원 학적정보(학년/반/번호) 변경 Mutation의 GraphQL 요청 DTO입니다.
 *
 * @param memberId 학적정보를 변경할 회원 ID
 * @param grade 변경할 학년, 학적정보를 비우려면 null
 * @param classNumber 변경할 반 번호, 학적정보를 비우려면 null
 * @param number 변경할 번호, 학적정보를 비우려면 null
 */
data class PatchMemberSchoolInfoInput(
    val memberId: Long,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
)
