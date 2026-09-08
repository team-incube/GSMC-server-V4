package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectDraftUseCase
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자의 프로젝트 초안 조회 유스케이스 구현 클래스입니다. */
@Port(direction = PortDirection.INBOUND)
class FetchMyProjectDraftService(
    private val projectDraftPersistencePort: ProjectDraftPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyProjectDraftUseCase {
    /** 현재 사용자의 초안을 조회하며, 초안이 없으면 null을 반환합니다. */
    @Transactional(readOnly = true)
    override fun execute(): ProjectDraft? {
        val userId = memberUtil.getCurrentUserId()
        return projectDraftPersistencePort.findByOwnerId(userId)
    }
}
