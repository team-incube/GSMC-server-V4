package team.incube.gsmc.domain.sheet

import team.incube.gsmc.domain.user.UserRole

data class SheetStudent(
    val userId: Long,
    val grade: Int,
    val classNumber: Int,
    val number: Int,
    val name: String,
    val role: UserRole,
)
