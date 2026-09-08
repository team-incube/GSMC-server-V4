@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.Project

/** 내부 프로젝트 생성을 담당하는 인바운드 유스케이스입니다. */
interface AppendProjectUseCase {
    /**
     * 현재 사용자를 소유자로 하여 프로젝트를 생성합니다.
     *
     * @param title 프로젝트 제목
     * @param description 프로젝트 설명
     * @param fileIds 연결할 파일 식별자 목록
     * @param participantIds 등록할 참여자 식별자 목록
     * @return 생성된 프로젝트
     */
    fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project
}
