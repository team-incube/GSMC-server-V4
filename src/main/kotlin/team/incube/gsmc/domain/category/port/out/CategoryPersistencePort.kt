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

    /**
     * 전체 카테고리를 categoryId 오름차순으로 조회한다.
     *
     * @return 전체 카테고리 목록
     */
    fun findAll(): List<Category>

    /**
     * 영문명 또는 한글명에 keyword가 포함된(대소문자 무시) 카테고리를 categoryId 오름차순으로 조회한다.
     *
     * @param keyword 검색 키워드
     * @return 매칭된 카테고리 목록
     */
    fun searchByKeyword(keyword: String): List<Category>
}
