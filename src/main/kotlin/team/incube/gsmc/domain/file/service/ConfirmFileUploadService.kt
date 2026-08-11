package team.incube.gsmc.domain.file.service

import org.springframework.transaction.annotation.Transactional
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.MAX_FILE_SIZE_BYTES
import team.incube.gsmc.domain.file.port.`in`.ConfirmFileUploadUseCase
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

/**
 * 파일 업로드 확인 유스케이스 구현 클래스입니다.
 * [ConfirmFileUploadUseCase]를 구현하며, 동일 key로 이미 confirm된 파일이 있는지, 오브젝트
 * 스토리지에서 실제 객체 존재/크기를 검증한 뒤 미연결 상태의 파일 메타데이터를 저장합니다.
 * `file_uri` 컬럼의 유니크 제약이 동시 요청에 대한 최후 방어선 역할을 합니다.
 */
@Port(direction = PortDirection.INBOUND)
class ConfirmFileUploadService(
    private val filePersistencePort: FilePersistencePort,
    private val fileStoragePort: FileStoragePort,
    private val memberUtil: MemberUtil,
) : ConfirmFileUploadUseCase {
    @Transactional
    override fun execute(
        fileKey: String,
        originalFileName: String,
    ): File {
        if (filePersistencePort.findByFileKey(fileKey) != null) throw GsmcException(ErrorCode.FILE_ALREADY_CONFIRMED)

        val objectSize = fileStoragePort.getObjectSize(fileKey) ?: throw GsmcException(ErrorCode.S3_OBJECT_NOT_FOUND)
        if (objectSize > MAX_FILE_SIZE_BYTES) throw GsmcException(ErrorCode.INVALID_FILE_SIZE)

        val file =
            File(
                fileId = 0,
                userId = memberUtil.getCurrentUserId(),
                fileKey = fileKey,
                fileOriginalName = originalFileName,
                fileStoredName = fileKey.substringAfterLast('/'),
            )

        return filePersistencePort.save(file)
    }
}
