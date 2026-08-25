package team.incube.gsmc.domain.category.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.QCategoryJpaEntity.categoryJpaEntity
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.category.adapter.out.persistence.repository.CategoryJpaRepository
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 카테고리 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [CategoryPersistencePort]를 구현하며, 단건 조회는 [CategoryJpaRepository]에, 목록 조회/검색은
 * QueryDSL(`JPAQueryFactory`)에 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class CategoryPersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
    private val categoryJpaRepository: CategoryJpaRepository,
) : CategoryPersistencePort {
    override fun findByCategoryType(categoryType: CategoryType): Category? =
        categoryJpaRepository.findByCategoryType(categoryType)?.toDomain()

    override fun findAll(): List<Category> =
        queryFactory
            .selectFrom(categoryJpaEntity)
            .orderBy(categoryJpaEntity.categoryId.asc())
            .fetch()
            .map { it.toDomain() }

    override fun searchByKeyword(keyword: String): List<Category> =
        queryFactory
            .selectFrom(categoryJpaEntity)
            .where(
                categoryJpaEntity.categoryEnglishName
                    .containsIgnoreCase(keyword)
                    .or(categoryJpaEntity.categoryKoreanName.containsIgnoreCase(keyword)),
            ).orderBy(categoryJpaEntity.categoryId.asc())
            .fetch()
            .map { it.toDomain() }
}
