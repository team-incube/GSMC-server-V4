package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectSummary
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectsUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자가 소유하거나 참여한 프로젝트 목록을 조회합니다. */
@Port(direction = PortDirection.INBOUND)
class FetchMyProjectsService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyProjectsUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<ProjectSummary> {
        val userId = memberUtil.getCurrentUserId()
        return projectPersistencePort.findAllByUserId(userId).map { ProjectSummary(it.projectId, it.title, it.ownerId) }
    }
}
