package team.incube.gsmc.domain.project.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.port.`in`.AppendProjectDraftUseCase
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 프로젝트 초안 저장 유스케이스 구현 클래스입니다.
 * 현재 사용자의 초안을 저장하며, 입력 내용과 파일 소유권을 검증한 뒤 기존 초안이 있으면 갱신합니다.
 */
@Port(direction = PortDirection.INBOUND)
class AppendProjectDraftService(
    private val projectDraftPersistencePort: ProjectDraftPersistencePort,
    private val projectServiceSupport: ProjectServiceSupport,
    private val memberUtil: MemberUtil,
) : AppendProjectDraftUseCase {
    /** 초안의 제목·설명과 연결할 참여자·파일을 검증한 뒤 저장합니다. */
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
