package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.Project

/** 내부 프로젝트 영속성을 추상화하는 아웃바운드 포트입니다. */
interface ProjectPersistencePort {
    fun findById(projectId: Long): Project?

    fun findAllByUserId(userId: Long): List<Project>

    fun findAllByTitleContaining(
        title: String,
        page: Int,
        size: Int,
    ): List<Project>

    fun countByTitleContaining(title: String): Long

    fun save(project: Project): Project

    fun deleteById(projectId: Long)
}
