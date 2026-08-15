package team.incube.gsmc.domain.file.service

import team.incube.gsmc.domain.file.port.`in`.GenerateFileAccessUrlUseCase
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port

/**
 * 파일 접근용 presigned URL 생성 유스케이스 구현 클래스입니다.
 * [GenerateFileAccessUrlUseCase]를 구현하며, 호출할 때마다 새 presigned GET URL을 발급합니다.
 */
@Port(direction = PortDirection.INBOUND)
class GenerateFileAccessUrlService(
    private val fileStoragePort: FileStoragePort,
) : GenerateFileAccessUrlUseCase {
    override fun execute(fileKey: String): String = fileStoragePort.createPresignedDownloadUrl(fileKey)
}
