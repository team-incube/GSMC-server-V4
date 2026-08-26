package team.incube.gsmc.domain.project.adapter.out.persistence.entity

import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.ProjectFile
import team.incube.gsmc.domain.project.ProjectParticipant
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/** Project JPA 엔티티를 순수 도메인 모델로 변환합니다. */
fun ProjectJpaEntity.toDomain(scoreIds: List<Long> = emptyList()): Project =
    Project(
        projectId = projectId,
        ownerId = owner.userId,
        title = title,
        description = description,
        participants = participants.map { ProjectParticipant(it.userId, it.userName) },
        files = files.map { ProjectFile(it.fileId, it.fileOriginalName, it.fileKey) },
        scoreIds = scoreIds,
    )

/** 관계를 초기화하지 않고 목록 조회용 Project 요약을 생성합니다. */
fun ProjectJpaEntity.toSummaryDomain(): Project =
    Project(
        projectId = projectId,
        ownerId = owner.userId,
        title = title,
        description = description,
    )

/** Project 도메인 모델을 JPA 엔티티로 변환합니다. */
fun Project.toEntity(
    owner: UserJpaEntity,
    participants: MutableSet<UserJpaEntity>,
    files: MutableSet<FileJpaEntity>,
): ProjectJpaEntity =
    ProjectJpaEntity(
        projectId = projectId,
        owner = owner,
        title = title,
        description = description,
        participants = participants,
        files = files,
    )
