@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

import team.incube.gsmc.domain.file.File

/**
 * 파일 단건 조회 유스케이스 인터페이스입니다.
 */
interface FetchFileUseCase {
    /**
     * ID로 파일 단건을 조회한다. 본인 소유이거나 교사(TEACHER) 이상만 조회할 수 있다.
     *
     * @param fileId 조회할 파일 ID
     * @return 해당 파일
     * @throws team.incube.gsmc.global.exception.GsmcException 파일이 없으면 [team.incube.gsmc.global.exception.ErrorCode.FILE_NOT_FOUND],
     * 접근 권한이 없으면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     */
    fun execute(fileId: Long): File
}
