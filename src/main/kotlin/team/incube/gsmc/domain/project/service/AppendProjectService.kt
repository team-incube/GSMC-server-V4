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

/**
 * 내부 프로젝트 생성 유스케이스 구현 클래스입니다.
 * 현재 사용자를 소유자로 지정하고, 소유자를 참여자에 자동 포함한 뒤 검증된 관계와 함께 저장합니다.
 */
@Port(direction = PortDirection.INBOUND)
class AppendProjectService(
    private val projectPersistencePort: ProjectPersistencePort,
    private val projectServiceSupport: ProjectServiceSupport,
    private val memberUtil: MemberUtil,
) : AppendProjectUseCase {
    /** 프로젝트 내용과 참여자·파일 소유권을 검증한 뒤 프로젝트를 생성합니다. */
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
