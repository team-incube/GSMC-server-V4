package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidencesUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 증빙자료 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchMyEvidencesUseCase]를 구현하며, 현재 사용자의 제출 완료 자료를 최신 생성순으로 조회합니다.
 * 자료 목록과 파일 연결은 사용자 범위로 조회하고 파일을 배치로 묶어 반환합니다.
 */
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
