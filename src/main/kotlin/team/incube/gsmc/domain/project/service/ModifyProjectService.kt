package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.ProjectFile
import team.incube.gsmc.domain.project.ProjectParticipant
import team.incube.gsmc.domain.project.port.`in`.ModifyProjectUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내부 프로젝트 수정 유스케이스 구현 클래스입니다.
 * 프로젝트 소유자만 수정할 수 있으며, 전달된 항목만 변경하고 생략된 관계는 기존 값을 유지합니다.
 */
@Port(direction = PortDirection.INBOUND)
class ModifyProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val projectServiceSupport: ProjectServiceSupport,
    private val memberUtil: MemberUtil,
) : ModifyProjectUseCase {
    /** 소유자 권한과 입력값을 검증한 뒤 프로젝트 내용을 부분 수정합니다. */
    @Transactional
    override fun execute(
        projectId: Long,
        title: String?,
        description: String?,
        fileIds: List<Long>?,
        participantIds: List<Long>?,
    ): Project {
        val ownerId = memberUtil.getCurrentUserId()
        val project = projectPersistencePort.findById(projectId) ?: throw GsmcException(ErrorCode.PROJECT_NOT_FOUND)
        if (project.ownerId != ownerId) throw GsmcException(ErrorCode.FORBIDDEN)
        projectServiceSupport.validatePatchContent(title, description)

        val participants =
            participantIds?.let {
                val ids = it.distinct()
                if (ownerId !in ids) throw GsmcException(ErrorCode.FORBIDDEN)
                projectServiceSupport.findParticipants(ids, ownerId)
            } ?: project.participants.map { it.id }.let { projectServiceSupport.findParticipants(it, ownerId) }
        val files =
            fileIds
                ?.let { projectServiceSupport.validateFiles(it, ownerId) }
                ?.map { ProjectFile(it.fileId, it.fileOriginalName, it.fileKey) }
                ?: project.files

        return projectPersistencePort.save(
            Project(
                projectId = project.projectId,
                ownerId = project.ownerId,
                title = title ?: project.title,
                description = description ?: project.description,
                participants = participants.map { ProjectParticipant(it.userId, it.userName) },
                files = files,
                scoreIds = project.scoreIds,
            ),
        )
    }
}
