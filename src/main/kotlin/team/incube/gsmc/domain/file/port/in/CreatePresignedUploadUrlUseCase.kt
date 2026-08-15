@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

import team.incube.gsmc.domain.file.PresignedUpload

/**
 * 파일 업로드용 presigned URL 발급 유스케이스 인터페이스입니다. DB에는 아무것도 기록하지 않으며,
 * 클라이언트는 발급받은 URL로 오브젝트 스토리지에 직접 업로드한 뒤 [ConfirmFileUploadUseCase]를
 * 호출해야 한다.
 */
interface CreatePresignedUploadUrlUseCase {
    /**
     * @param fileName 업로드할 파일의 원본 파일명
     * @param fileSize 업로드할 파일의 크기(byte)
     * @param contentType 업로드할 파일의 MIME 타입
     * @return 발급된 presigned URL, 객체 key, 만료 시각
     * @throws team.incube.gsmc.global.exception.GsmcException [fileSize]가 허용 크기를 초과하면
     * [team.incube.gsmc.global.exception.ErrorCode.INVALID_FILE_SIZE]
     */
    fun execute(
        fileName: String,
        fileSize: Long,
        contentType: String,
    ): PresignedUpload
}
