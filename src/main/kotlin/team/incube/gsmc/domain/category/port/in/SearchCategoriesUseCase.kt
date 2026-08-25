@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.category.port.`in`

import team.incube.gsmc.domain.category.Category

/**
 * 키워드 기반 카테고리 검색 유스케이스 인터페이스입니다.
 */
interface SearchCategoriesUseCase {
    /**
     * 영문명 또는 한글명에 keyword가 포함된(대소문자 무시) 카테고리를 조회한다.
     * keyword가 null이거나 blank이면 전체 카테고리를 반환한다.
     *
     * @param keyword 검색 키워드
     * @return categoryId 오름차순으로 정렬된 카테고리 목록
     */
    fun execute(keyword: String?): List<Category>
}
