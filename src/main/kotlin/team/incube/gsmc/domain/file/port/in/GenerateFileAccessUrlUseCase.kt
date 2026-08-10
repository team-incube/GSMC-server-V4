@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

/**
 * 파일 접근용 presigned URL 생성 유스케이스 인터페이스입니다. GraphQL `File.fileUri` 필드
 * 리졸버가 호출할 때마다 새로 발급한다.
 */
interface GenerateFileAccessUrlUseCase {
    /**
     * @param fileKey 오브젝트 스토리지 key(파일 도메인 객체의 `fileUri` 값)
     * @return 발급된 presigned GET URL
     */
    fun execute(fileKey: String): String
}
