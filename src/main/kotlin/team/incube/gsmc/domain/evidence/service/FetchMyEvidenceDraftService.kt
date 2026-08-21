package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자의 증빙자료 임시저장을 조회하는 서비스입니다. */
@Port(direction = PortDirection.INBOUND)
class FetchMyEvidenceDraftService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val evidenceServiceSupport: EvidenceServiceSupport,
    private val memberUtil: MemberUtil,
) : FetchMyEvidenceDraftUseCase {
    @Transactional(readOnly = true)
    override fun execute(): Evidence? =
        evidencePersistencePort.findDraftByUserId(memberUtil.getCurrentUserId())?.let(evidenceServiceSupport::withFiles)
}
