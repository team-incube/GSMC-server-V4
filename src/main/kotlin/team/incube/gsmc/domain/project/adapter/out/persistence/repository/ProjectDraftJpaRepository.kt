package team.incube.gsmc.domain.project.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.project.adapter.out.persistence.entity.ProjectDraftJpaEntity

/** 내부 프로젝트 초안 JPA 저장소입니다. */
interface ProjectDraftJpaRepository : JpaRepository<ProjectDraftJpaEntity, Long>
