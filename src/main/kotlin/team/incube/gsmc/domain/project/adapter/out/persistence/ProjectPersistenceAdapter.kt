package team.incube.gsmc.domain.project.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.ProjectJpaEntity
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.QProjectJpaEntity.projectJpaEntity
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.toSummaryDomain
import team.incube.gsmc.domain.project.adapter.out.persistence.repository.ProjectJpaRepository
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.QUserJpaEntity.userJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/** 내부 프로젝트의 JPA 영속성을 담당하는 아웃바운드 어댑터입니다. */
@Adapter(direction = PortDirection.OUTBOUND)
class ProjectPersistenceAdapter(
    private val projectJpaRepository: ProjectJpaRepository,
    private val entityManager: EntityManager,
    private val queryFactory: JPAQueryFactory,
) : ProjectPersistencePort {
    override fun findById(projectId: Long): Project? =
        queryFactory
            .selectFrom(projectJpaEntity)
            .where(projectJpaEntity.projectId.eq(projectId))
            .fetchOne()
            ?.let { it.toDomain(findScoreIds(projectId)) }

    override fun findAllByUserId(userId: Long): List<Project> {
        val entities =
            queryFactory
                .selectFrom(projectJpaEntity)
                .distinct()
                .leftJoin(projectJpaEntity.participants, userJpaEntity)
                .where(
                    projectJpaEntity.owner.userId
                        .eq(userId)
                        .or(userJpaEntity.userId.eq(userId)),
                ).orderBy(projectJpaEntity.projectId.desc())
                .fetch()
        return entities.map { it.toSummaryDomain() }
    }

    override fun findAllByTitleContaining(
        title: String,
        page: Int,
        size: Int,
    ): List<Project> =
        queryFactory
            .selectFrom(projectJpaEntity)
            .where(projectJpaEntity.title.containsIgnoreCase(title))
            .orderBy(projectJpaEntity.projectId.desc())
            .offset((page * size).toLong())
            .limit(size.toLong())
            .fetch()
            .map { it.toSummaryDomain() }

    override fun countByTitleContaining(title: String): Long =
        queryFactory
            .select(projectJpaEntity.count())
            .from(projectJpaEntity)
            .where(projectJpaEntity.title.containsIgnoreCase(title))
            .fetchOne() ?: 0L

    override fun save(project: Project): Project {
        val owner = entityManager.getReference(UserJpaEntity::class.java, project.ownerId)
        val participants =
            project.participants
                .map { entityManager.getReference(UserJpaEntity::class.java, it.id) }
                .toMutableSet()
        val files =
            project.files
                .map { entityManager.getReference(FileJpaEntity::class.java, it.id) }
                .toMutableSet()
        val saved = projectJpaRepository.save(project.toEntity(owner, participants, files))
        return findById(saved.projectId) ?: project.copy(projectId = saved.projectId)
    }

    override fun deleteById(projectId: Long) {
        projectJpaRepository.deleteById(projectId)
    }

    private fun findScoreIds(projectId: Long): List<Long> =
        queryFactory
            .select(scoreJpaEntity.scoreId)
            .from(scoreJpaEntity)
            .where(scoreJpaEntity.project.projectId.eq(projectId))
            .fetch()
}
