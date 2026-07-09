package team.incube.gsmc.domain.category.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity

/**
 * 카테고리 정보에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface CategoryJpaRepository : JpaRepository<CategoryJpaEntity, Long> {
    /**
     * 카테고리 유형으로 카테고리 엔티티를 조회한다.
     *
     * @param categoryType 조회할 카테고리 유형
     * @return 해당 카테고리 엔티티, 없으면 null
     */
    fun findByCategoryType(categoryType: CategoryType): CategoryJpaEntity?
}
