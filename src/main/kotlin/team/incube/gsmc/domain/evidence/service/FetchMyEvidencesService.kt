package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidencesUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자의 제출 완료 증빙자료 목록을 최신순으로 조회하는 서비스입니다. */
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
