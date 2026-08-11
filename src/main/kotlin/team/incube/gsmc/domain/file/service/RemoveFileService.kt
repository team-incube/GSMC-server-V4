package team.incube.gsmc.domain.file.service

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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
 * 여부와 무관하게 삭제합니다. DB row를 먼저 삭제하고, 트랜잭션이 커밋된 뒤에 오브젝트
 * 스토리지 객체를 삭제합니다. 스토리지 삭제는 트랜잭션 롤백 대상이 아니므로, DB 삭제(커밋)가
 * 실패해도 스토리지 객체는 그대로 남아 깨진 링크가 발생하지 않습니다. 커밋 후 스토리지 삭제가
 * 실패하면 고아 객체만 남을 수 있습니다.
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

        filePersistencePort.deleteById(fileId)

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    fileStoragePort.deleteObject(file.fileKey)
                }
            },
        )

        return true
    }
}
