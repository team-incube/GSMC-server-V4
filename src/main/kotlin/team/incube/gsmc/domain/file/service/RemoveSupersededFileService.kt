package team.incube.gsmc.domain.file.service

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.`in`.RemoveSupersededFileUseCase
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port

/**
 * 밀려난 점수에 딸린 파일 정리 유스케이스 구현 클래스입니다.
 * [RemoveSupersededFileUseCase]를 구현하며, 소유자·승인 가드 없이 삭제합니다.
 *
 * DB row를 먼저 삭제하고 트랜잭션이 커밋된 뒤에 스토리지 객체를 삭제하는 순서는
 * [RemoveFileService]와 같습니다. 스토리지 삭제는 롤백 대상이 아니므로 DB 삭제(커밋)가 실패해도
 * 스토리지 객체가 그대로 남아 깨진 링크가 생기지 않습니다. 커밋 후 스토리지 삭제가 실패하면
 * 고아 객체만 남으며, 이 경우 로그로 남깁니다.
 */
@Port(direction = PortDirection.INBOUND)
class RemoveSupersededFileService(
    private val filePersistencePort: FilePersistencePort,
    private val fileStoragePort: FileStoragePort,
) : RemoveSupersededFileUseCase {
    private val log = LoggerFactory.getLogger(RemoveSupersededFileService::class.java)

    @Transactional
    override fun execute(file: File) {
        filePersistencePort.deleteById(file.fileId)

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    try {
                        fileStoragePort.deleteObject(file.fileKey)
                    } catch (e: Exception) {
                        log.error(
                            "스토리지 객체 삭제 실패로 고아 객체가 남았습니다. fileId={}, fileKey={}",
                            file.fileId,
                            file.fileKey,
                            e,
                        )
                    }
                }
            },
        )
    }
}
