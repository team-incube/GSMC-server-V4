package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidencesUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

@Port(direction = PortDirection.INBOUND)
class FetchMyEvidencesService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val evidenceServiceSupport: EvidenceServiceSupport,
    private val memberUtil: MemberUtil,
) : FetchMyEvidencesUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Evidence> =
        evidenceServiceSupport.withFiles(
            evidencePersistencePort.findAllByUserId(memberUtil.getCurrentUserId()),
        )
}
