@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectDraft

/** 현재 사용자의 프로젝트 초안을 저장하는 인바운드 유스케이스입니다. */
interface AppendProjectDraftUseCase {
    /**
     * 프로젝트 초안을 저장하거나 기존 초안을 갱신합니다.
     *
     * @param title 초안 제목
     * @param description 초안 설명
     * @param fileIds 연결할 파일 식별자 목록
     * @param participantIds 등록할 참여자 식별자 목록
     * @return 저장된 프로젝트 초안
     */
    fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): ProjectDraft
}
