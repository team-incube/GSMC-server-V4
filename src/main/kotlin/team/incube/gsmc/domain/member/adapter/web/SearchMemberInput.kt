package team.incube.gsmc.domain.member.adapter.web

import team.incube.gsmc.domain.member.SortDirection
import team.incube.gsmc.domain.user.UserRole

data class SearchMemberInput(
    val email: String?,
    val name: String?,
    val role: UserRole?,
    val grade: Int?,
    val classNumber: Int?,
    val number: Int?,
    val limit: Int = 100,
    val page: Int = 0,
    val sort: SortDirection = SortDirection.ASC,
)
