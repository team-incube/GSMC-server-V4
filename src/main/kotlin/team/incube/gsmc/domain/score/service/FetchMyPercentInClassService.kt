package team.incube.gsmc.domain.score.service

import team.incube.gsmc.domain.score.Percentile
import team.incube.gsmc.domain.score.ScoreAggregator
import team.incube.gsmc.domain.score.port.`in`.FetchMyPercentInClassUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 학급 내 백분위 조회 유스케이스 구현 클래스입니다.
 * [FetchMyPercentInClassUseCase]를 구현하며, 같은 반 학생 전체를 모집단으로 백분위를 계산합니다.
 * 학생(STUDENT)만 호출할 수 있습니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyPercentInClassService(
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyPercentInClassUseCase {
    override fun execute(includeApprovedOnly: Boolean): Percentile {
        if (memberUtil.getCurrentUserRole() != UserRole.STUDENT) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val userId = memberUtil.getCurrentUserId()
        val me = memberPersistencePort.findByUserId(userId) ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
        val userGrade = requireNotNull(me.userGrade) { "학생은 userGrade가 null일 수 없습니다." }
        val userClassNumber = requireNotNull(me.userClassNumber) { "학생은 userClassNumber가 null일 수 없습니다." }

        val classmateIds =
            memberPersistencePort
                .findAllStudentsByUserGradeAndUserClassNumber(userGrade, userClassNumber)
                .map { it.userId }
                .toSet() + userId
        val scoresByUserId = scorePersistencePort.findAllByUserIdIn(classmateIds.toList()).groupBy { it.userId }
        val totalScoreByUserId =
            classmateIds.associateWith { id ->
                ScoreAggregator.totalScoreOf(scoresByUserId[id] ?: emptyList(), includeApprovedOnly)
            }

        return ScoreAggregator.percentileOf(userId, totalScoreByUserId)
    }
}
