package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.Project

/** 내부 프로젝트 영속성을 추상화하는 아웃바운드 포트입니다. */
interface ProjectPersistencePort {
    /** 프로젝트 식별자로 프로젝트 상세를 조회합니다. */
    fun findById(projectId: Long): Project?

    /** 사용자가 소유하거나 참여한 프로젝트를 조회합니다. */
    fun findAllByUserId(userId: Long): List<Project>

    /** 제목에 검색어가 포함된 프로젝트를 페이지 단위로 조회합니다. */
    fun findAllByTitleContaining(
        title: String,
        page: Int,
        size: Int,
    ): List<Project>

    /** 제목 검색 조건에 해당하는 프로젝트 전체 개수를 조회합니다. */
    fun countByTitleContaining(title: String): Long

    /** 프로젝트를 저장하거나 갱신합니다. */
    fun save(project: Project): Project

    /** 프로젝트를 식별자로 삭제합니다. */
    fun deleteById(projectId: Long)
}
