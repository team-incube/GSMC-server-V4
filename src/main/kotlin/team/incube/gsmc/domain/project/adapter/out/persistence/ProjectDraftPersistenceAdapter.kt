package team.incube.gsmc.domain.project.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.ProjectDraftJpaEntity
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.QProjectDraftJpaEntity.projectDraftJpaEntity
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.project.adapter.out.persistence.repository.ProjectDraftJpaRepository
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/** 내부 프로젝트 초안의 JPA 영속성을 담당하는 아웃바운드 어댑터입니다. */
@Adapter(direction = PortDirection.OUTBOUND)
class ProjectDraftPersistenceAdapter(
    private val projectDraftJpaRepository: ProjectDraftJpaRepository,
    private val entityManager: EntityManager,
    private val queryFactory: JPAQueryFactory,
) : ProjectDraftPersistencePort {
    /** 소유자 식별자로 프로젝트 초안을 조회합니다. */
    override fun findByOwnerId(ownerId: Long): ProjectDraft? =
        queryFactory
            .selectFrom(projectDraftJpaEntity)
            .where(projectDraftJpaEntity.user.userId.eq(ownerId))
            .fetchOne()
            ?.toDomain()

    /** 프로젝트 초안을 신규 저장하거나 기존 초안으로 갱신합니다. */
    override fun save(
        ownerId: Long,
        draft: ProjectDraft,
    ): ProjectDraft {
        val existing =
            queryFactory
                .selectFrom(projectDraftJpaEntity)
                .where(projectDraftJpaEntity.user.userId.eq(ownerId))
                .fetchOne()
        val user = entityManager.getReference(UserJpaEntity::class.java, ownerId)
        val participants =
            draft.participantIds
                .map { entityManager.getReference(UserJpaEntity::class.java, it) }
                .toMutableSet()
        val files =
            draft.fileIds
                .map { entityManager.getReference(FileJpaEntity::class.java, it) }
                .toMutableSet()
        val entity =
            ProjectDraftJpaEntity(
                projectDraftId = existing?.projectDraftId ?: 0,
                user = user,
                title = draft.title,
                description = draft.description,
                participants = participants,
                files = files,
            )
        projectDraftJpaRepository.save(entity)
        return draft
    }

    /** 소유자 식별자에 해당하는 프로젝트 초안을 삭제합니다. */
    override fun deleteByOwnerId(ownerId: Long) {
        queryFactory
            .selectFrom(projectDraftJpaEntity)
            .where(projectDraftJpaEntity.user.userId.eq(ownerId))
            .fetchOne()
            ?.let { projectDraftJpaRepository.delete(it) }
    }
}
