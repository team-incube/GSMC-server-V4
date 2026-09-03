package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.port.`in`.RemoveProjectDraftUseCase
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자의 프로젝트 초안 삭제 유스케이스 구현 클래스입니다. */
@Port(direction = PortDirection.INBOUND)
class RemoveProjectDraftService(
    private val projectDraftPersistencePort: ProjectDraftPersistencePort,
    private val memberUtil: MemberUtil,
) : RemoveProjectDraftUseCase {
    /** 현재 사용자의 프로젝트 초안을 삭제합니다. */
    @Transactional
    override fun execute(): Boolean {
        projectDraftPersistencePort.deleteByOwnerId(memberUtil.getCurrentUserId())
        return true
    }
}
