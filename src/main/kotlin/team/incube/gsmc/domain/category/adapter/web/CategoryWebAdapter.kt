package team.incube.gsmc.domain.category.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.category.port.`in`.FetchCategoriesUseCase
import team.incube.gsmc.domain.category.port.`in`.SearchCategoriesUseCase

/**
 * 카테고리 조회/검색 GraphQL Query 리졸버입니다.
 * Query를 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class CategoryWebAdapter(
    private val fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val searchCategoriesUseCase: SearchCategoriesUseCase,
) {
    @QueryMapping
    fun categories(): List<CategoryPayload> = fetchCategoriesUseCase.execute().map { it.toPayload() }

    @QueryMapping
    fun searchCategories(
        @Argument keyword: String?,
    ): List<CategoryPayload> = searchCategoriesUseCase.execute(keyword).map { it.toPayload() }
}
