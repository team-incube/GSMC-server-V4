package team.incube.gsmc.domain.project.service

import team.incube.gsmc.domain.project.DataGsmProject
import team.incube.gsmc.domain.project.port.`in`.FetchMyWritableDataGsmProjectsUseCase
import team.incube.gsmc.domain.project.port.out.DataGsmProjectApiPort
import team.incube.gsmc.domain.project.port.out.ProjectMemberPersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내가 참여자로 등록된 DataGSM ACTIVE 프로젝트 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchMyWritableDataGsmProjectsUseCase]를 구현하며, 학생(STUDENT)만 호출을 허용합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyWritableDataGsmProjectsService(
    private val dataGsmProjectApiPort: DataGsmProjectApiPort,
    private val projectMemberPersistencePort: ProjectMemberPersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyWritableDataGsmProjectsUseCase {
    override fun execute(): List<DataGsmProject> {
        if (memberUtil.getCurrentUserRole() != UserRole.STUDENT) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        val user =
            projectMemberPersistencePort.findByUserId(memberUtil.getCurrentUserId())
                ?: throw GsmcException(ErrorCode.USER_NOT_FOUND)

        return dataGsmProjectApiPort.findActiveProjectsByParticipantEmail(user.userEmail)
    }
}
