package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.port.`in`.RemoveEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

@Port(direction = PortDirection.INBOUND)
class RemoveEvidenceDraftService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val filePersistencePort: FilePersistencePort,
    private val memberUtil: MemberUtil,
) : RemoveEvidenceDraftUseCase {
    @Transactional
    override fun execute(): Boolean {
        val evidence = evidencePersistencePort.findDraftByUserId(memberUtil.getCurrentUserId()) ?: return true
        filePersistencePort.unlinkAllFromEvidence(evidence.evidenceId)
        evidencePersistencePort.deleteById(evidence.evidenceId)
        return true
    }
}
