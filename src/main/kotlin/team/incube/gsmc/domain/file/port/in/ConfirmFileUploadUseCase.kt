@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

import team.incube.gsmc.domain.file.File

/**
 * 파일 업로드 확인 유스케이스 인터페이스입니다. 클라이언트가 presigned URL로 오브젝트
 * 스토리지에 업로드를 마친 뒤 호출하며, 실제 객체 존재 여부를 확인한 다음 파일 메타데이터를
 * 저장한다. 저장 직후에는 어떤 점수 요청/근거 자료와도 연결되지 않은 미연결 상태다.
 */
interface ConfirmFileUploadUseCase {
    /**
     * @param fileKey [CreatePresignedUploadUrlUseCase]가 발급한 오브젝트 스토리지 key
     * @param originalFileName 사용자가 업로드한 원본 파일명
     * @return 저장된 파일
     * @throws team.incube.gsmc.global.exception.GsmcException 오브젝트 스토리지에 해당 key의 객체가 없으면
     * [team.incube.gsmc.global.exception.ErrorCode.S3_OBJECT_NOT_FOUND], 객체 크기가 허용 크기를 초과하면
     * [team.incube.gsmc.global.exception.ErrorCode.INVALID_FILE_SIZE]
     */
    fun execute(
        fileKey: String,
        originalFileName: String,
    ): File
}
