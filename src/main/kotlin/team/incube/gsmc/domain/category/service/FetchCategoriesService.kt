package team.incube.gsmc.domain.category.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.port.`in`.FetchCategoriesUseCase
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port

/**
 * 전체 카테고리 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchCategoriesUseCase]를 구현하며, 조회를 [CategoryPersistencePort]에 위임합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchCategoriesService(
    private val categoryPersistencePort: CategoryPersistencePort,
) : FetchCategoriesUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Category> = categoryPersistencePort.findAll()
}
