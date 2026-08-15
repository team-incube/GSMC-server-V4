package team.incube.gsmc.domain.file.service

import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.`in`.FetchFileUseCase
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.user.isTeacherOrAbove
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 파일 단건 조회 유스케이스 구현 클래스입니다.
 * [FetchFileUseCase]를 구현하며, 본인 소유이거나 교사(TEACHER) 이상만 조회를 허용합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchFileService(
    private val filePersistencePort: FilePersistencePort,
    private val memberUtil: MemberUtil,
) : FetchFileUseCase {
    override fun execute(fileId: Long): File {
        val file = filePersistencePort.findById(fileId) ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)

        val currentUserId = memberUtil.getCurrentUserId()
        if (file.userId != currentUserId && !memberUtil.getCurrentUserRole().isTeacherOrAbove()) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }

        return file
    }
}
