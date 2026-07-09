package team.incube.gsmc.domain.category.port.out

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType

/**
 * 카테고리 영속성을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface CategoryPersistencePort {
    /**
     * 카테고리 유형으로 카테고리를 조회한다.
     *
     * @param categoryType 조회할 카테고리 유형
     * @return 해당 카테고리, 없으면 null
     */
    fun findByCategoryType(categoryType: CategoryType): Category?
}
