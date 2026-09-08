@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectSummary

/** 현재 사용자가 소유하거나 참여한 프로젝트 조회를 담당하는 인바운드 유스케이스입니다. */
interface FetchMyProjectsUseCase {
    /** @return 현재 사용자의 프로젝트 요약 목록 */
    fun execute(): List<ProjectSummary>
}
