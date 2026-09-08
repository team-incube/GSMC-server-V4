@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

/** 현재 사용자의 프로젝트 초안 삭제를 담당하는 인바운드 유스케이스입니다. */
interface RemoveProjectDraftUseCase {
    /** @return 초안 삭제 성공 여부 */
    fun execute(): Boolean
}
