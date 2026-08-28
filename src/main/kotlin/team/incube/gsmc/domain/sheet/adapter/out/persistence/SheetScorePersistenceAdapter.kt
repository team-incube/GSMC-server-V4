package team.incube.gsmc.domain.sheet.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.sheet.port.out.SheetScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/** 학생별 승인 점수를 한 번에 조회하는 영속성 어댑터입니다. */
@Adapter(direction = PortDirection.OUTBOUND)
class SheetScorePersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
) : SheetScorePersistencePort {
    /** 회원 식별자 목록에 해당하는 승인 점수를 회원별로 묶어 조회합니다. */
    override fun findApprovedScoresByUserIds(userIds: Collection<Long>): Map<Long, List<Score>> {
        if (userIds.isEmpty()) return emptyMap()

        return queryFactory
            .selectFrom(scoreJpaEntity)
            .join(scoreJpaEntity.user)
            .fetchJoin()
            .join(scoreJpaEntity.category)
            .fetchJoin()
            .leftJoin(scoreJpaEntity.evidence)
            .fetchJoin()
            .where(
                scoreJpaEntity.user.userId.`in`(userIds),
                scoreJpaEntity.scoreStatus.eq(ScoreStatus.APPROVED),
            ).fetch()
            .map { it.toDomain(null) }
            .groupBy { it.userId }
    }
}
