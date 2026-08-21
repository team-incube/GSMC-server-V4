package team.incube.gsmc.domain.evidence.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.AppendEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

@Port(direction = PortDirection.INBOUND)
class AppendEvidenceDraftService(
    private val evidencePersistencePort: EvidencePersistencePort,
    private val evidenceServiceSupport: EvidenceServiceSupport,
    private val memberUtil: MemberUtil,
) : AppendEvidenceDraftUseCase {
    @Transactional
    override fun execute(
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence {
        val userId = memberUtil.getCurrentUserId()
        evidenceServiceSupport.validateDraftContent(title, content)
        val existing = evidencePersistencePort.findDraftByUserId(userId)
        val uniqueFileIds = fileIds.distinct()
        val evidence =
            if (existing == null) {
                evidenceServiceSupport.validateAndFindFiles(uniqueFileIds, userId)
                evidencePersistencePort.save(
                    Evidence(
                        evidenceId = 0,
                        userId = userId,
                        evidenceTitle = title,
                        evidenceContent = content,
                        evidenceCreatedAt = null,
                        evidenceUpdatedAt = null,
                        isDraft = true,
                    ),
                )
            } else {
                val existingFileIds =
                    evidenceServiceSupport
                        .withFiles(existing)
                        .files
                        .map { it.fileId }
                        .toSet()
                evidenceServiceSupport.validateAndFindFiles(uniqueFileIds, userId, existing.evidenceId)
                evidencePersistencePort
                    .save(existing.copy(evidenceTitle = title, evidenceContent = content))
                    .also {
                        evidenceServiceSupport.syncFiles(existing.evidenceId, existingFileIds, uniqueFileIds)
                    }
            }

        if (existing == null) {
            evidenceServiceSupport.syncFiles(evidence.evidenceId, emptyList(), uniqueFileIds)
        }
        return evidenceServiceSupport.withFiles(evidence)
    }
}
