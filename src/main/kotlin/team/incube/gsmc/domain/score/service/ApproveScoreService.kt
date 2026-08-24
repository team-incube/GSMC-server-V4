package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.port.out.AlertEventPublisherPort
import team.incube.gsmc.domain.alert.port.out.AlertPersistencePort
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
 * 검증하지 않고 무조건 `APPROVED`로 갈아끼웁니다. 단, 알림은 이미 `APPROVED`인 점수를 다시 승인할
 * 때 중복 생성되지 않도록 실제로 상태가 바뀐 경우에만 저장하며, 상태 변경과 알림 저장은 같은
 * 트랜잭션으로 묶여 둘 중 하나만 반영되는 일이 없습니다. 알림 저장 직후 [AlertEventPublisherPort]로
 * SSE 실시간 전달을 요청하지만, 실제 전송은 이 트랜잭션이 Commit된 이후에만 이뤄진다. 상태가 실제로
 * 바뀐 경우에만 해당 학생의 반/학년 백분위 캐시([ScoreTotalCacheInvalidator])를 무효화한다.
 */
@Port(direction = PortDirection.INBOUND)
class ApproveScoreService(
    private val scorePersistencePort: ScorePersistencePort,
    private val alertPersistencePort: AlertPersistencePort,
    private val alertEventPublisherPort: AlertEventPublisherPort,
    private val scoreTotalCacheInvalidator: ScoreTotalCacheInvalidator,
    private val memberUtil: MemberUtil,
) : ApproveScoreUseCase {
    @Transactional
    override fun execute(scoreId: Long): Boolean {
        if (!memberUtil.getCurrentUserRole().isTeacherOrAbove()) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val score = scorePersistencePort.findById(scoreId) ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        val alreadyApproved = score.scoreStatus == ScoreStatus.APPROVED
        scorePersistencePort.save(score.copy(scoreStatus = ScoreStatus.APPROVED, rejectionReason = null))

        if (!alreadyApproved) {
            scoreTotalCacheInvalidator.invalidate(score.userId)
            val savedAlert =
                alertPersistencePort.save(
                    Alert.approved(
                        userId = score.userId,
                        scoreId = score.scoreId,
                        categoryName = score.category.categoryKoreanName,
                    ),
                )
            alertEventPublisherPort.publish(savedAlert)
        }

        return true
    }
}
