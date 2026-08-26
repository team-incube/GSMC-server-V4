package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.port.`in`.AppendProjectDraftUseCase
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 사용자별 프로젝트 초안을 생성하거나 갱신합니다. */
@Port(direction = PortDirection.INBOUND)
class AppendProjectDraftService(
    private val projectDraftPersistencePort: ProjectDraftPersistencePort,
    private val projectServiceSupport: ProjectServiceSupport,
    private val memberUtil: MemberUtil,
) : AppendProjectDraftUseCase {
    @Transactional
    override fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): ProjectDraft {
        val ownerId = memberUtil.getCurrentUserId()
        projectServiceSupport.validateDraftContent(title, description)
        val participants = projectServiceSupport.findParticipants(participantIds, ownerId, includeOwner = false)
        val files = projectServiceSupport.validateFiles(fileIds, ownerId)
        return projectDraftPersistencePort.save(
            ownerId = ownerId,
            draft =
                ProjectDraft(
                    title = title,
                    description = description,
                    participantIds = participants.map { it.userId },
                    fileIds = files.map { it.fileId },
                ),
        )
    }
}
