package team.incube.gsmc.domain.category.service

import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.port.`in`.SearchCategoriesUseCase
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port

/**
 * 키워드 기반 카테고리 검색 유스케이스 구현 클래스입니다.
 * [SearchCategoriesUseCase]를 구현하며, keyword가 없으면 전체 목록을, 있으면 검색 결과를
 * [CategoryPersistencePort]에서 조회합니다.
 */
@Port(direction = PortDirection.INBOUND)
class SearchCategoriesService(
    private val categoryPersistencePort: CategoryPersistencePort,
) : SearchCategoriesUseCase {
    override fun execute(keyword: String?): List<Category> =
        if (keyword.isNullOrBlank()) {
            categoryPersistencePort.findAll()
        } else {
            categoryPersistencePort.searchByKeyword(keyword)
        }
}
