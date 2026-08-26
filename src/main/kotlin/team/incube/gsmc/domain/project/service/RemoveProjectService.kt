package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.port.`in`.RemoveProjectUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/** 내부 프로젝트를 삭제하고 Score·File·S3 원본은 보존합니다. */
@Port(direction = PortDirection.INBOUND)
class RemoveProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : RemoveProjectUseCase {
    @Transactional
    override fun execute(projectId: Long): Boolean {
        val userId = memberUtil.getCurrentUserId()
        val project = projectPersistencePort.findById(projectId) ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
        if (project.ownerId != userId) throw GsmcException(ErrorCode.FORBIDDEN)
        scorePersistencePort.unlinkProject(projectId)
        projectPersistencePort.deleteById(projectId)
        return true
    }
}
