package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자가 소유한 제출 완료 증빙자료를 단건 조회하는 서비스입니다. */
@Port(direction = PortDirection.INBOUND)
class FetchEvidenceService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val evidenceServiceSupport: EvidenceServiceSupport,
    private val memberUtil: MemberUtil,
) : FetchEvidenceUseCase {
    @Transactional(readOnly = true)
    override fun execute(evidenceId: Long): Evidence {
        val evidence = evidencePersistencePort.findById(evidenceId)
        if (evidence == null || evidence.userId != memberUtil.getCurrentUserId() || evidence.isDraft) {
            throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)
        }
        return evidenceServiceSupport.withFiles(evidence)
    }
}
