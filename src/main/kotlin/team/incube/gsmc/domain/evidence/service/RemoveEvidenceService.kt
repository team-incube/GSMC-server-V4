package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.port.`in`.RemoveEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자가 소유한 증빙자료를 연관관계와 함께 삭제하는 서비스입니다. */
@Port(direction = PortDirection.INBOUND)
class RemoveEvidenceService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val scorePersistencePort: ScorePersistencePort,
    private val filePersistencePort: FilePersistencePort,
    private val memberUtil: MemberUtil,
) : RemoveEvidenceUseCase {
    @Transactional
    override fun execute(evidenceId: Long): Boolean {
        val evidence = evidencePersistencePort.findById(evidenceId)
        if (evidence == null || evidence.userId != memberUtil.getCurrentUserId() || evidence.isDraft) {
            throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)
        }
        scorePersistencePort.unlinkEvidence(evidenceId)
        filePersistencePort.unlinkAllFromEvidence(evidenceId)
        evidencePersistencePort.deleteById(evidenceId)
        return true
    }
}
