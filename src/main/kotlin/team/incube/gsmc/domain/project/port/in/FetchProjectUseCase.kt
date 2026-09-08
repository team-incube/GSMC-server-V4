@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.Project

/** 내부 프로젝트 단건 조회를 담당하는 인바운드 유스케이스입니다. */
interface FetchProjectUseCase {
    /**
     * 프로젝트 상세를 조회합니다.
     *
     * @param projectId 조회할 프로젝트 식별자
     * @return 조회한 프로젝트
     */
    fun execute(projectId: Long): Project
}
