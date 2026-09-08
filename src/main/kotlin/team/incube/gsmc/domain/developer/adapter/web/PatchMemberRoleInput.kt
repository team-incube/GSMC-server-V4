package team.incube.gsmc.domain.developer.adapter.web

import team.incube.gsmc.domain.user.UserRole

/**
 * 회원 역할 변경 Mutation의 GraphQL 요청 DTO입니다.
 *
 * @param email 역할을 변경할 회원의 이메일
 * @param role 변경할 역할
 */
data class PatchMemberRoleInput(
    val email: String,
    val role: UserRole,
)
