package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.port.`in`.RemoveEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 증빙자료 임시저장 삭제 유스케이스 구현 클래스입니다.
 * [RemoveEvidenceDraftUseCase]를 구현하며, 현재 사용자의 임시저장과 File 연결만 해제합니다.
 * 임시저장이 없어도 멱등적으로 true를 반환하며, File 레코드와 스토리지 객체는 삭제하지 않습니다.
 */
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
