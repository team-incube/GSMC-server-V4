package team.incube.gsmc.domain.file.service

import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.`in`.FetchMyFilesUseCase
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.util.MemberUtil

/**
 * 내 파일 목록 조회 유스케이스 구현 클래스입니다.
 * [FetchMyFilesUseCase]를 구현하며, 현재 로그인한 사용자가 업로드한 파일을 전부 조회합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchMyFilesService(
    private val filePersistencePort: FilePersistencePort,
    private val memberUtil: MemberUtil,
) : FetchMyFilesUseCase {
    override fun execute(): List<File> = filePersistencePort.findAllByUserId(memberUtil.getCurrentUserId())
}
