package team.incube.gsmc.domain.sheet.adapter.out.persistence

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.sheet.port.out.SheetScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

@Adapter(direction = PortDirection.OUTBOUND)
class SheetScorePersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
) : SheetScorePersistencePort {
    override fun findApprovedTotalScoreByUserIds(userIds: Collection<Long>): Map<Long, Int> {
        if (userIds.isEmpty()) return emptyMap()

        val userIdExpression = scoreJpaEntity.user.userId
        val totalScoreExpression: NumberExpression<Int> =
            Expressions.numberTemplate(Int::class.java, "sum({0})", scoreJpaEntity.scoreValue)

        return queryFactory
            .select(userIdExpression, totalScoreExpression)
            .from(scoreJpaEntity)
            .where(
                userIdExpression.`in`(userIds),
                scoreJpaEntity.scoreStatus.eq(ScoreStatus.APPROVED),
                scoreJpaEntity.scoreValue.isNotNull,
            ).groupBy(userIdExpression)
            .fetch()
            .mapNotNull { tuple ->
                val userId = tuple.get(userIdExpression) ?: return@mapNotNull null
                userId to (tuple.get(totalScoreExpression) ?: 0)
            }.toMap()
    }
}
