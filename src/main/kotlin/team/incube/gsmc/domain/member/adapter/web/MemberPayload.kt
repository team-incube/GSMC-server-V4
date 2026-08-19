package team.incube.gsmc.domain.member.adapter.web

import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole

/**
 * 회원 단건 조회 Query의 GraphQL 응답 DTO입니다.
 * 도메인 [User]와 필드명이 달라(`userId` → `id` 등) 별도로 매핑합니다.
 */
data class MemberPayload(
    val id: Long,
    val name: String,
    val email: String,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
    val role: UserRole,
)

/**
 * 도메인 모델 [User]를 [MemberPayload]로 변환한다.
 *
 * @receiver 변환할 도메인 객체
 * @return 변환된 [MemberPayload]
 */
fun User.toPayload(): MemberPayload =
    MemberPayload(
        id = userId,
        name = userName,
        email = userEmail,
        grade = userGrade,
        classNumber = userClassNumber,
        number = userNumber,
        role = userRole,
    )
