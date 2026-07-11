package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.ApproveScoreUseCase
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.isTeacherOrAbove
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 점수 승인 유스케이스 구현 클래스입니다.
 * [ApproveScoreUseCase]를 구현하며, 교사(TEACHER) 이상만 호출을 허용합니다. 상태 전이 유효성은
 * 검증하지 않고 무조건 `APPROVED`로 갈아끼웁니다.
 */
@Port(direction = PortDirection.INBOUND)
class ApproveScoreService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : ApproveScoreUseCase {
    @Transactional
    override fun execute(scoreId: Long): Boolean {
        if (!memberUtil.getCurrentUserRole().isTeacherOrAbove()) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val score = scorePersistencePort.findById(scoreId) ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        scorePersistencePort.save(score.copy(scoreStatus = ScoreStatus.APPROVED, rejectionReason = null))

        return true
    }
}
