package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.`in`.RejectScoreUseCase
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.isTeacherOrAbove
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

private const val MAX_REJECTION_REASON_LENGTH = 500

/**
 * 점수 거절 유스케이스 구현 클래스입니다.
 * [RejectScoreUseCase]를 구현하며, 교사(TEACHER) 이상만 호출을 허용합니다. `rejectionReason`은
 * `score_tb.rejection_reason` 컬럼 길이(500자)를 초과하면 DB 예외 대신 명확한 에러로 미리 막는다.
 */
@Port(direction = PortDirection.INBOUND)
class RejectScoreService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : RejectScoreUseCase {
    @Transactional
    override fun execute(
        scoreId: Long,
        rejectionReason: String,
    ): Boolean {
        if (!memberUtil.getCurrentUserRole().isTeacherOrAbove()) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }
        if (rejectionReason.isBlank() || rejectionReason.length > MAX_REJECTION_REASON_LENGTH) {
            throw GsmcException(ErrorCode.INVALID_REJECTION_REASON)
        }

        val score = scorePersistencePort.findById(scoreId) ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        scorePersistencePort.save(score.copy(scoreStatus = ScoreStatus.REJECTED, rejectionReason = rejectionReason))

        return true
    }
}
