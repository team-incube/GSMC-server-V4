package team.incube.gsmc.domain.project.adapter.web

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.project.DataGsmProject
import team.incube.gsmc.domain.project.port.`in`.FetchMyWritableDataGsmProjectsUseCase

/**
 * DataGSM 프로젝트 조회 GraphQL Query 리졸버입니다.
 * Query를 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class ProjectWebAdapter(
    private val fetchMyWritableDataGsmProjectsUseCase: FetchMyWritableDataGsmProjectsUseCase,
) {
    @QueryMapping
    fun myWritableDataGsmProjects(): List<DataGsmProject> = fetchMyWritableDataGsmProjectsUseCase.execute()
}
