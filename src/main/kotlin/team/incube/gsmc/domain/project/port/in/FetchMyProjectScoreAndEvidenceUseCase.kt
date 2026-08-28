@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectScoreAndEvidence

/** 프로젝트 참여자인 현재 사용자의 점수와 증빙자료 조회를 담당하는 인바운드 유스케이스입니다. */
interface FetchMyProjectScoreAndEvidenceUseCase {
    /**
     * 프로젝트에 연결된 현재 사용자의 점수와 증빙자료를 조회합니다.
     *
     * @param projectId 조회할 프로젝트 식별자
     * @return 현재 사용자의 점수와 증빙자료
     */
    fun execute(projectId: Long): ProjectScoreAndEvidence
}
