package team.incube.gsmc.domain.score.service

import org.springframework.stereotype.Component
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.canAccessScoresOf
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 현재 사용자가 [targetUserId]의 점수를 조회할 권한이 있는지 검증하는 컴포넌트입니다.
 * [team.incube.gsmc.domain.user.canAccessScoresOf]로 판단하며, 권한이 없으면 [ErrorCode.FORBIDDEN]을 던집니다.
 * 역할을 먼저 확인해 불필요한 조회를 줄이며, [UserRole.HOMEROOM_TEACHER]만 담당 학급 일치 여부를 위해
 * 현재 사용자 정보를 추가로 조회합니다.
 */
@Component
class ScoreAccessValidator(
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) {
    fun validate(targetUserId: Long) {
        val currentUserRole = memberUtil.getCurrentUserRole()

        if (currentUserRole == UserRole.STUDENT || currentUserRole == UserRole.UNAUTHORIZED) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val target = memberPersistencePort.findByUserId(targetUserId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)

        if (currentUserRole == UserRole.TEACHER || currentUserRole == UserRole.ROOT) {
            return
        }

        val currentUser =
            memberPersistencePort.findByUserId(memberUtil.getCurrentUserId())
                ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        if (!currentUser.canAccessScoresOf(target)) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }
    }
}
