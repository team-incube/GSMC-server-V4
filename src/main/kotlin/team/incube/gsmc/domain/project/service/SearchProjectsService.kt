package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectSearchResult
import team.incube.gsmc.domain.project.ProjectSummary
import team.incube.gsmc.domain.project.port.`in`.SearchProjectsUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

private const val MAX_PAGE_SIZE = 100

/** 프로젝트 제목 부분 일치 검색과 데이터베이스 페이지 처리를 담당합니다. */
@Port(direction = PortDirection.INBOUND)
class SearchProjectsService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val memberUtil: MemberUtil,
) : SearchProjectsUseCase {
    @Transactional(readOnly = true)
    override fun execute(
        title: String,
        page: Int,
        size: Int,
    ): ProjectSearchResult {
        memberUtil.getCurrentUserId()
        if (page < 0) throw GsmcException(ErrorCode.INVALID_PAGE)
        if (size !in 1..MAX_PAGE_SIZE) throw GsmcException(ErrorCode.INVALID_PAGE_SIZE)
        val projects = projectPersistencePort.findAllByTitleContaining(title, page, size)
        val totalElements = projectPersistencePort.countByTitleContaining(title)
        return ProjectSearchResult(
            totalElements = totalElements,
            projects = projects.map { ProjectSummary(it.projectId, it.title, it.ownerId) },
        )
    }
}
