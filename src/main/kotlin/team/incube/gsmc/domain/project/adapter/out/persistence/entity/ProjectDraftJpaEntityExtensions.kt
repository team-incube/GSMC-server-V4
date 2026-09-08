package team.incube.gsmc.domain.project.adapter.out.persistence.entity

import team.incube.gsmc.domain.project.ProjectDraft

/** Project 초안 JPA 엔티티를 도메인 모델로 변환합니다. */
fun ProjectDraftJpaEntity.toDomain(): ProjectDraft =
    ProjectDraft(
        title = title,
        description = description,
        participantIds = participants.map { it.userId },
        fileIds = files.map { it.fileId },
    )
