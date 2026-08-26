package team.incube.gsmc.domain.project.service

import org.springframework.stereotype.Component
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.project.port.out.ProjectMemberPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

private const val MIN_TITLE_LENGTH = 1
private const val MAX_TITLE_LENGTH = 100
private const val MIN_DESCRIPTION_LENGTH = 300
private const val MAX_DESCRIPTION_LENGTH = 2000
private const val MAX_DRAFT_TITLE_LENGTH = 255
private const val MAX_DRAFT_DESCRIPTION_LENGTH = 65_535

/** Project 서비스에서 공통으로 사용하는 입력·관계 검증 기능입니다. */
@Component
class ProjectServiceSupport(
    private val projectMemberPersistencePort: ProjectMemberPersistencePort,
    private val filePersistencePort: FilePersistencePort,
) {
    fun validateFinalContent(
        title: String,
        description: String,
    ) {
        if (title.length !in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH ||
            description.length !in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH
        ) {
            throw GsmcException(ErrorCode.INVALID_PROJECT_INPUT)
        }
    }

    fun validatePatchContent(
        title: String?,
        description: String?,
    ) {
        title?.let { if (it.length !in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH) invalidInput() }
        description?.let {
            if (it.length !in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) invalidInput()
        }
    }

    fun validateDraftContent(
        title: String,
        description: String,
    ) {
        if (title.length > MAX_DRAFT_TITLE_LENGTH || description.length > MAX_DRAFT_DESCRIPTION_LENGTH) {
            invalidInput()
        }
    }

    fun findParticipants(
        participantIds: Collection<Long>,
        ownerId: Long,
        includeOwner: Boolean = true,
    ): List<User> {
        val ids = if (includeOwner) (participantIds + ownerId).distinct() else participantIds.distinct()
        val members = projectMemberPersistencePort.findAllByUserIds(ids)
        if (members.size != ids.size) throw GsmcException(ErrorCode.USER_NOT_FOUND)
        return ids.map { id -> members.first { it.userId == id } }
    }

    fun validateFiles(
        fileIds: Collection<Long>,
        ownerId: Long,
    ): List<File> {
        val ids = fileIds.distinct()
        val files = filePersistencePort.findAllByIdIn(ids)
        if (files.size != ids.size) throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        if (files.any { it.userId != ownerId }) throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        return ids.map { id -> files.first { it.fileId == id } }
    }

    private fun invalidInput(): Nothing = throw GsmcException(ErrorCode.INVALID_PROJECT_INPUT)
}
