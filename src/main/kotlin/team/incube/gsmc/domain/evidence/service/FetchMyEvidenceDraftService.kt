package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 증빙자료 임시저장 조회 유스케이스 구현 클래스입니다.
 * [FetchMyEvidenceDraftUseCase]를 구현하며, 현재 사용자의 임시저장만 조회하고 연결된 파일을 함께 반환합니다.
 * 임시저장이 없으면 null을 반환합니다.
 */
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
