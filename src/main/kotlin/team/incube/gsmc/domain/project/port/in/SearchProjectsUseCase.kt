@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectSearchResult

/** 프로젝트 제목 검색을 담당하는 인바운드 유스케이스입니다. */
interface SearchProjectsUseCase {
    /**
     * 제목에 검색어가 포함된 프로젝트를 페이지 단위로 조회합니다.
     *
     * @param title 검색할 제목
     * @param page 조회할 페이지 번호
     * @param size 페이지 크기
     * @return 검색 결과와 전체 결과 수
     */
    fun execute(
        title: String,
        page: Int,
        size: Int,
    ): ProjectSearchResult
}
