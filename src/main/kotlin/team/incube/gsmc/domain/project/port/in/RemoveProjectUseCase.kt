@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

/** 내부 프로젝트 삭제를 담당하는 인바운드 유스케이스입니다. */
interface RemoveProjectUseCase {
    /**
     * 프로젝트 소유자가 프로젝트를 삭제합니다.
     *
     * @param projectId 삭제할 프로젝트 식별자
     * @return 프로젝트 삭제 성공 여부
     */
    fun execute(projectId: Long): Boolean
}
