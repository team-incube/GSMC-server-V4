@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.Project

/** 내부 프로젝트 수정을 담당하는 인바운드 유스케이스입니다. */
interface ModifyProjectUseCase {
    /**
     * 프로젝트 소유자가 프로젝트의 지정된 항목을 수정합니다.
     *
     * @param projectId 수정할 프로젝트 식별자
     * @param title 변경할 제목, null이면 기존 값 유지
     * @param description 변경할 설명, null이면 기존 값 유지
     * @param fileIds 변경할 파일 식별자 목록, null이면 기존 연결 유지
     * @param participantIds 변경할 참여자 식별자 목록, null이면 기존 연결 유지
     * @return 수정된 프로젝트
     */
    fun execute(
        projectId: Long,
        title: String?,
        description: String?,
        fileIds: List<Long>?,
        participantIds: List<Long>?,
    ): Project
}
