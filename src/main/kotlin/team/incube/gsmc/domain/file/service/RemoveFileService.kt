package team.incube.gsmc.domain.file.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.file.port.`in`.RemoveFileUseCase
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 파일 삭제 유스케이스 구현 클래스입니다.
 * [RemoveFileUseCase]를 구현하며, 소유자 본인만 호출을 허용합니다. 점수 요청/근거 자료 연결
 * 여부와 무관하게 삭제합니다. 오브젝트 스토리지 객체를 먼저 삭제한 뒤 DB row를 삭제하며,
 * 스토리지 삭제가 실패하면 예외가 그대로 전파되어 트랜잭션 전체가 롤백됩니다.
 */
@Port(direction = PortDirection.INBOUND)
class RemoveFileService(
    private val filePersistencePort: FilePersistencePort,
    private val fileStoragePort: FileStoragePort,
    private val memberUtil: MemberUtil,
) : RemoveFileUseCase {
    @Transactional
    override fun execute(fileId: Long): Boolean {
        val file = filePersistencePort.findById(fileId) ?: throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        if (file.userId != memberUtil.getCurrentUserId()) throw GsmcException(ErrorCode.FORBIDDEN)

        fileStoragePort.deleteObject(file.fileUri)
        filePersistencePort.deleteById(fileId)

        return true
    }
}
