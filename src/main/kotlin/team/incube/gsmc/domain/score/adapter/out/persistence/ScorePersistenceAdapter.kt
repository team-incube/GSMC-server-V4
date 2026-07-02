package team.incube.gsmc.domain.score.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.QFileJpaEntity.fileJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 점수 요청 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [ScorePersistencePort]를 구현하며, QueryDSL로 category/evidence를 fetch join하여 조회합니다.
 * `user`는 식별자(userId)만 필요하므로 fetch join하지 않습니다 — 지연 로딩 프록시에서도 식별자 조회는
 * 초기화를 유발하지 않습니다.
 * [ScoreJpaEntity]는 [team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity]에 대한
 * 참조가 없어(역방향 연관관계), 첨부 파일은 별도 쿼리로 조회 후 병합합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class ScorePersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
) : ScorePersistencePort {
    override fun findById(scoreId: Long): Score? {
        val entity =
            queryFactory
                .selectFrom(scoreJpaEntity)
                .join(scoreJpaEntity.category)
                .fetchJoin()
                .leftJoin(scoreJpaEntity.evidence)
                .fetchJoin()
                .where(scoreJpaEntity.scoreId.eq(scoreId))
                .fetchOne() ?: return null

        return entity.toDomain(findFileByScoreId(scoreId))
    }

    override fun findAllByUserId(userId: Long): List<Score> = findAllByUserIdIn(listOf(userId))

    override fun findAllByUserIdIn(userIds: List<Long>): List<Score> {
        if (userIds.isEmpty()) return emptyList()

        val entities =
            queryFactory
                .selectFrom(scoreJpaEntity)
                .join(scoreJpaEntity.category)
                .fetchJoin()
                .leftJoin(scoreJpaEntity.evidence)
                .fetchJoin()
                .where(scoreJpaEntity.user.userId.`in`(userIds))
                .fetch()

        if (entities.isEmpty()) return emptyList()

        val filesByScoreId =
            queryFactory
                .selectFrom(fileJpaEntity)
                .join(fileJpaEntity.score)
                .fetchJoin()
                .where(fileJpaEntity.score.scoreId.`in`(entities.map { it.scoreId }))
                .fetch()
                .groupBy { it.score!!.scoreId }

        return entities.map { entity ->
            entity.toDomain(filesByScoreId[entity.scoreId]?.firstOrNull()?.toDomain())
        }
    }

    private fun findFileByScoreId(scoreId: Long) =
        queryFactory
            .selectFrom(fileJpaEntity)
            .where(fileJpaEntity.score.scoreId.eq(scoreId))
            .fetchFirst()
            ?.toDomain()
}
