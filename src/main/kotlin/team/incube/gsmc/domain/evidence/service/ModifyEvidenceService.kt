package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.ModifyEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/** 현재 사용자가 소유한 증빙자료의 내용과 파일 연결을 수정하는 서비스입니다. */
@Port(direction = PortDirection.INBOUND)
class ModifyEvidenceService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val evidenceServiceSupport: EvidenceServiceSupport,
    private val memberUtil: MemberUtil,
) : ModifyEvidenceUseCase {
    @Transactional
    override fun execute(
        evidenceId: Long,
        title: String?,
        content: String?,
        fileIds: List<Long>?,
    ): Evidence {
        val userId = memberUtil.getCurrentUserId()
        val evidence = evidencePersistencePort.findById(evidenceId)
        if (evidence == null || evidence.userId != userId || evidence.isDraft) {
            throw GsmcException(ErrorCode.EVIDENCE_NOT_FOUND)
        }

        val updatedTitle = title ?: evidence.evidenceTitle
        val updatedContent = content ?: evidence.evidenceContent
        evidenceServiceSupport.validateFinalContent(updatedTitle, updatedContent)

        val existingFileIds =
            evidenceServiceSupport
                .withFiles(evidence)
                .files
                .map { it.fileId }
                .toSet()
        val requestedFileIds = fileIds?.distinct() ?: existingFileIds.toList()
        evidenceServiceSupport.validateAndFindFiles(requestedFileIds, userId, evidenceId)
        val updated =
            evidencePersistencePort.save(
                evidence.copy(evidenceTitle = updatedTitle, evidenceContent = updatedContent),
            )
        evidenceServiceSupport.syncFiles(evidenceId, existingFileIds, requestedFileIds)
        return evidenceServiceSupport.withFiles(updated)
    }
}
