package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.port.`in`.FetchProjectUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/** 인증된 사용자가 내부 프로젝트 상세를 조회합니다. */
@Port(direction = PortDirection.INBOUND)
class FetchProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchProjectUseCase {
    @Transactional(readOnly = true)
    override fun execute(projectId: Long): Project {
        memberUtil.getCurrentUserId()
        return projectPersistencePort.findById(projectId) ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
    }
}
