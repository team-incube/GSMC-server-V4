package team.incube.gsmc.domain.category.adapter.out.persistence

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.category.adapter.out.persistence.repository.CategoryJpaRepository
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 카테고리 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [CategoryPersistencePort]를 구현하며, 카테고리 조회 기능을 [CategoryJpaRepository]에 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class CategoryPersistenceAdapter(
    private val categoryJpaRepository: CategoryJpaRepository,
) : CategoryPersistencePort {
    override fun findByCategoryType(categoryType: CategoryType): Category? =
        categoryJpaRepository.findByCategoryType(categoryType)?.toDomain()
}
