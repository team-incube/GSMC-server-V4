package team.incube.gsmc.global.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.auth.CustomUserDetails
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

@Component
class MemberUtil {
    fun getCurrentUserId(): Long = currentUserDetails().userId

    fun getCurrentUserRole(): UserRole = currentUserDetails().userRole

    private fun currentUserDetails(): CustomUserDetails =
        SecurityContextHolder.getContext().authentication?.principal as? CustomUserDetails
            ?: throw GsmcException(ErrorCode.INVALID_TOKEN)
}
