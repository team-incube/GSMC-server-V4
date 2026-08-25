@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.category.port.`in`

import team.incube.gsmc.domain.category.Category

/**
 * 전체 카테고리 목록 조회 유스케이스 인터페이스입니다.
 */
interface FetchCategoriesUseCase {
    /**
     * @return categoryId 오름차순으로 정렬된 전체 카테고리 목록
     */
    fun execute(): List<Category>
}
