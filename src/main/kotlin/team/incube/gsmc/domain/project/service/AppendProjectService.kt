package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.ProjectFile
import team.incube.gsmc.domain.project.ProjectParticipant
import team.incube.gsmc.domain.project.port.`in`.AppendProjectUseCase
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/** 내부 프로젝트를 생성하고 owner를 참여자에 자동 포함합니다. */
@Port(direction = PortDirection.INBOUND)
class AppendProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val projectServiceSupport: ProjectServiceSupport,
    private val memberUtil: MemberUtil,
) : AppendProjectUseCase {
    @Transactional
    override fun execute(
        title: String,
        description: String,
        fileIds: List<Long>,
        participantIds: List<Long>,
    ): Project {
        val ownerId = memberUtil.getCurrentUserId()
        projectServiceSupport.validateFinalContent(title, description)
        val participants = projectServiceSupport.findParticipants(participantIds, ownerId)
        val files = projectServiceSupport.validateFiles(fileIds, ownerId)
        return projectPersistencePort.save(
            Project(
                projectId = 0,
                ownerId = ownerId,
                title = title,
                description = description,
                participants = participants.map { ProjectParticipant(it.userId, it.userName) },
                files = files.map { ProjectFile(it.fileId, it.fileOriginalName, it.fileKey) },
            ),
        )
    }
}
