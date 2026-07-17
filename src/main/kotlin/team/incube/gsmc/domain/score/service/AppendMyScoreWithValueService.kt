package team.incube.gsmc.domain.score.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.converter.ScoreValueConverterRegistry
import team.incube.gsmc.domain.score.port.`in`.AppendMyScoreWithValueUseCase
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 값 기반 점수 추가 유스케이스 구현 클래스입니다.
 * [AppendMyScoreWithValueUseCase]를 구현하며, 증빙이 필요 없고 집계 방식이 SCORE_BASED인
 * 카테고리에 대해 숫자 값을 입력해 점수를 신청한다. 교과성적([team.incube.gsmc.domain.category.CategoryType.ACADEMIC_GRADE])은
 * 학생 학년별 등급제 범위를 벗어나지 않는지 추가로 검증한다.
 */
@Port(direction = PortDirection.INBOUND)
class AppendMyScoreWithValueService(
    private val appendScoreSupport: AppendScoreSupport,
    private val scorePersistencePort: ScorePersistencePort,
    private val memberPersistencePort: MemberPersistencePort,
    private val memberUtil: MemberUtil,
) : AppendMyScoreWithValueUseCase {
    @Transactional
    override fun execute(
        categoryType: CategoryType,
        value: String?,
    ): Score {
        val userId = memberUtil.getCurrentUserId()
        val category = appendScoreSupport.resolveUnrequiredCategory(categoryType, ScoreCalculationType.SCORE_BASED)

        if (categoryType == CategoryType.ACADEMIC_GRADE) {
            val studentGrade =
                memberPersistencePort.findByUserId(userId)?.userGrade
                    ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)
            val rawValue = value?.trim()?.toDoubleOrNull() ?: throw GsmcException(ErrorCode.INVALID_SCORE_VALUE)
            ScoreValueConverterRegistry.resolve(categoryType).validate(rawValue, studentGrade)
        }

        val scoreValue = appendScoreSupport.parseScoreValue(value, category)

        val target = appendScoreSupport.findOrCreateScore(userId, category)
        return scorePersistencePort.save(
            target.copy(
                scoreStatus = ScoreStatus.PENDING,
                activityName = null,
                scoreValue = scoreValue,
                rejectionReason = null,
            ),
        )
    }
}
