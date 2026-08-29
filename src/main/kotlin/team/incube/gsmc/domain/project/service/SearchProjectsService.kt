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

/**
 * 프로젝트 제목 부분 일치 검색 유스케이스 구현 클래스입니다.
 * 페이지 번호와 크기를 검증한 뒤 제목 검색 결과와 전체 개수를 함께 반환합니다.
 */
@Port(direction = PortDirection.INBOUND)
class SearchProjectsService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val memberUtil: MemberUtil,
) : SearchProjectsUseCase {
    /** 제목 검색어와 페이지 조건을 검증하고 프로젝트 요약 목록을 조회합니다. */
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
