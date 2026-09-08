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

/** 인증된 사용자의 내부 프로젝트 상세 조회 유스케이스 구현 클래스입니다. */
@Port(direction = PortDirection.INBOUND)
class FetchProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchProjectUseCase {
    /** 인증된 사용자가 프로젝트 상세를 조회하며, 없는 프로젝트는 예외로 처리합니다. */
    @Transactional(readOnly = true)
    override fun execute(projectId: Long): Project {
        memberUtil.getCurrentUserId()
        return projectPersistencePort.findById(projectId) ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
    }
}
