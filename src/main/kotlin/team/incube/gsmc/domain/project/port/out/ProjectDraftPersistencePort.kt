package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.ProjectDraft

/** 내부 프로젝트 초안 영속성을 추상화하는 아웃바운드 포트입니다. */
interface ProjectDraftPersistencePort {
    /** 소유자 식별자로 프로젝트 초안을 조회합니다. */
    fun findByOwnerId(ownerId: Long): ProjectDraft?

    /** 소유자의 프로젝트 초안을 저장하거나 갱신합니다. */
    fun save(
        ownerId: Long,
        draft: ProjectDraft,
    ): ProjectDraft

    /** 소유자의 프로젝트 초안을 삭제합니다. */
    fun deleteByOwnerId(ownerId: Long)
}
