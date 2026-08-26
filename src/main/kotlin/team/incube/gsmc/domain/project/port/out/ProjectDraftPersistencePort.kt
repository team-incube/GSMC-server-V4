package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.ProjectDraft

/** 내부 프로젝트 초안 영속성을 추상화하는 아웃바운드 포트입니다. */
interface ProjectDraftPersistencePort {
    fun findByOwnerId(ownerId: Long): ProjectDraft?

    fun save(
        ownerId: Long,
        draft: ProjectDraft,
    ): ProjectDraft

    fun deleteByOwnerId(ownerId: Long)
}
