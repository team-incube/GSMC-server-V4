package team.incube.gsmc.global.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.incube.gsmc.global.auth.CustomUserDetails
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

@Component
class MemberUtil {
    fun getCurrentUserId(): Long =
        (SecurityContextHolder.getContext().authentication?.principal as? CustomUserDetails)?.userId
            ?: throw GsmcException(ErrorCode.INVALID_TOKEN)
}
