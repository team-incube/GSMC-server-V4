package team.incube.gsmc.domain.score.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.QFileJpaEntity.fileJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.repository.ScoreJpaRepository
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 점수 요청 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [ScorePersistencePort]를 구현하며, QueryDSL로 user/category/evidence를 fetch join하여 조회합니다.
 * Kotlin + JPA는 기본적으로 FIELD Access를 사용하므로, 지연 로딩 프록시의 식별자(`user.userId`)를
 * 읽는 것만으로도 초기화(추가 쿼리)가 유발될 수 있어 user도 fetch join 대상에 포함합니다.
 * [ScoreJpaEntity]는 [team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity]에 대한
 * 참조가 없어(역방향 연관관계), 첨부 파일은 별도 쿼리로 조회 후 병합합니다.
 * 저장/삭제는 [ScoreJpaRepository]에, FK 참조 조립은 [EntityManager.getReference]에 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class ScorePersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
    private val scoreJpaRepository: ScoreJpaRepository,
    private val entityManager: EntityManager,
) : ScorePersistencePort {
    override fun findById(scoreId: Long): Score? {
        val entity =
            queryFactory
                .selectFrom(scoreJpaEntity)
                .join(scoreJpaEntity.user)
                .fetchJoin()
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
                .join(scoreJpaEntity.user)
                .fetchJoin()
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

    override fun findByUserIdAndCategoryTypeAndScoreStatus(
        userId: Long,
        categoryType: CategoryType,
        scoreStatus: ScoreStatus,
    ): Score? {
        val entity =
            queryFactory
                .selectFrom(scoreJpaEntity)
                .join(scoreJpaEntity.user)
                .fetchJoin()
                .join(scoreJpaEntity.category)
                .fetchJoin()
                .leftJoin(scoreJpaEntity.evidence)
                .fetchJoin()
                .where(
                    scoreJpaEntity.user.userId.eq(userId),
                    scoreJpaEntity.category.categoryType.eq(categoryType),
                    scoreJpaEntity.scoreStatus.eq(scoreStatus),
                ).fetchOne() ?: return null

        return entity.toDomain(findFileByScoreId(entity.scoreId))
    }

    override fun save(score: Score): Score {
        val user = entityManager.getReference(UserJpaEntity::class.java, score.userId)
        val category = entityManager.getReference(CategoryJpaEntity::class.java, score.category.categoryId)
        val evidence = score.evidence?.let { entityManager.getReference(EvidenceJpaEntity::class.java, it.evidenceId) }

        val saved = scoreJpaRepository.save(score.toEntity(user, category, evidence))
        return score.copy(
            scoreId = saved.scoreId,
            createdAt = saved.createdAt,
            updatedAt = saved.updatedAt,
        )
    }

    override fun deleteById(scoreId: Long) {
        scoreJpaRepository.deleteById(scoreId)
    }

    private fun findFileByScoreId(scoreId: Long) =
        queryFactory
            .selectFrom(fileJpaEntity)
            .where(fileJpaEntity.score.scoreId.eq(scoreId))
            .fetchFirst()
            ?.toDomain()
}
