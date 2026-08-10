@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

/**
 * 파일 삭제 유스케이스 인터페이스입니다. 소유자 본인만 호출할 수 있으며, 점수 요청/근거 자료
 * 연결 여부와 무관하게 삭제할 수 있다.
 */
interface RemoveFileUseCase {
    /**
     * @param fileId 삭제할 파일 ID
     * @return 처리 성공 여부
     * @throws team.incube.gsmc.global.exception.GsmcException 파일이 없으면 [team.incube.gsmc.global.exception.ErrorCode.FILE_NOT_FOUND],
     * 소유자가 아니면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     */
    fun execute(fileId: Long): Boolean
}
