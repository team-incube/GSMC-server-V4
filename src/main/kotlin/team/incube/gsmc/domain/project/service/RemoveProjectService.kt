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

/**
 * 내부 프로젝트 삭제 유스케이스 구현 클래스입니다.
 * 프로젝트 소유자만 삭제할 수 있으며, 연결된 점수 관계만 해제하고 파일과 저장소 원본은 보존합니다.
 */
@Port(direction = PortDirection.INBOUND)
class RemoveProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val scorePersistencePort: ScorePersistencePort,
    private val memberUtil: MemberUtil,
) : RemoveProjectUseCase {
    /** 소유자 권한을 확인하고 프로젝트와 점수의 연결을 삭제합니다. */
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
