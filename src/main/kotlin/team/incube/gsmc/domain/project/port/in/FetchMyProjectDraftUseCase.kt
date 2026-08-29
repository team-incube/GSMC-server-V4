@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectDraft

/** 현재 사용자의 프로젝트 초안 조회를 담당하는 인바운드 유스케이스입니다. */
interface FetchMyProjectDraftUseCase {
    /** @return 현재 사용자의 프로젝트 초안, 없으면 null */
    fun execute(): ProjectDraft?
}
