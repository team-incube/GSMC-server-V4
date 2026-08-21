package team.incube.gsmc.domain.evidence.service

import org.springframework.stereotype.Component
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

private const val MIN_TITLE_LENGTH = 1
private const val MAX_TITLE_LENGTH = 100
private const val MIN_CONTENT_LENGTH = 300
private const val MAX_CONTENT_LENGTH = 2000
private const val MAX_DRAFT_TITLE_LENGTH = 255
private const val MAX_DRAFT_CONTENT_LENGTH = 65_535

/** Evidence 서비스에서 공통으로 사용하는 입력값 검증과 파일 연결 기능을 제공합니다. */
@Component
class EvidenceServiceSupport(
    private val filePersistencePort: FilePersistencePort,
) {
    fun validateFinalContent(
        title: String,
        content: String,
    ) {
        if (title.length !in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH ||
            content.length !in MIN_CONTENT_LENGTH..MAX_CONTENT_LENGTH
        ) {
            throw GsmcException(ErrorCode.INVALID_EVIDENCE_INPUT)
        }
    }

    fun validateDraftContent(
        title: String,
        content: String,
    ) {
        if (title.length > MAX_DRAFT_TITLE_LENGTH || content.length > MAX_DRAFT_CONTENT_LENGTH) {
            throw GsmcException(ErrorCode.INVALID_EVIDENCE_INPUT)
        }
    }

    fun validateAndFindFiles(
        fileIds: Collection<Long>,
        userId: Long,
        allowedEvidenceId: Long? = null,
    ): List<File> {
        val uniqueFileIds = fileIds.toSet()
        val files = filePersistencePort.findAllByIdIn(uniqueFileIds)
        if (files.size != uniqueFileIds.size) throw GsmcException(ErrorCode.FILE_NOT_FOUND)

        files.forEach { file ->
            if (file.userId != userId) throw GsmcException(ErrorCode.FILE_NOT_FOUND)
            if (file.scoreId != null || (file.evidenceId != null && file.evidenceId != allowedEvidenceId)) {
                throw GsmcException(ErrorCode.FILE_ALREADY_LINKED)
            }
        }
        return files
    }

    fun syncFiles(
        evidenceId: Long,
        existingFileIds: Collection<Long>,
        requestedFileIds: Collection<Long>,
    ) {
        val existing = existingFileIds.toSet()
        val requested = requestedFileIds.toSet()
        existing.filterNot { it in requested }.forEach { filePersistencePort.unlinkFromEvidence(it) }
        requested.filterNot { it in existing }.forEach { filePersistencePort.linkToEvidence(it, evidenceId) }
    }

    fun withFiles(evidence: Evidence): Evidence =
        evidence.copy(files = filePersistencePort.findAllByEvidenceId(evidence.evidenceId))

    fun withFiles(evidences: List<Evidence>): List<Evidence> {
        if (evidences.isEmpty()) return emptyList()
        val filesByEvidenceId =
            filePersistencePort
                .findAllByEvidenceIdIn(evidences.map { it.evidenceId })
                .filter { it.evidenceId != null }
                .groupBy { it.evidenceId!! }
        return evidences.map { it.copy(files = filesByEvidenceId[it.evidenceId].orEmpty()) }
    }
}
