package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.project.ProjectScoreAndEvidence
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectScoreAndEvidenceUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 프로젝트 참여자인 현재 사용자의 점수와 증빙자료 조회 유스케이스 구현 클래스입니다.
 * 프로젝트 참여 여부를 확인한 뒤 현재 사용자의 점수와 연결된 증빙자료·파일만 반환합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyProjectScoreAndEvidenceService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val scorePersistencePort: ScorePersistencePort,
    private val evidencePersistencePort: EvidencePersistencePort,
    private val filePersistencePort: FilePersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyProjectScoreAndEvidenceUseCase {
    /** 프로젝트 참여 권한을 확인하고 현재 사용자의 점수와 증빙자료를 조회합니다. */
    @Transactional(readOnly = true)
    override fun execute(projectId: Long): ProjectScoreAndEvidence {
        val userId = memberUtil.getCurrentUserId()
        val project = projectPersistencePort.findById(projectId) ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
        if (project.participants.none { it.id == userId }) throw GsmcException(ErrorCode.FORBIDDEN)
        val score =
            scorePersistencePort.findByUserIdAndProjectId(userId, projectId)
                ?: throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        val evidence =
            score.evidence
                ?.let { evidencePersistencePort.findById(it.evidenceId) }
                ?.let { it.copy(files = filePersistencePort.findAllByEvidenceId(it.evidenceId)) }
                ?.let { listOf(it) }
                .orEmpty()
        return ProjectScoreAndEvidence(score, evidence)
    }
}
