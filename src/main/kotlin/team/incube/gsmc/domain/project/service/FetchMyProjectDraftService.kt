package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectDraftUseCase
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자의 프로젝트 초안을 조회합니다. */
@Port(direction = PortDirection.INBOUND)
class FetchMyProjectDraftService(
    private val projectDraftPersistencePort: ProjectDraftPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyProjectDraftUseCase {
    @Transactional(readOnly = true)
    override fun execute(): ProjectDraft? {
        val userId = memberUtil.getCurrentUserId()
        return projectDraftPersistencePort.findByOwnerId(userId)
    }
}
