package team.incube.gsmc.domain.file

import java.time.Instant

/**
 * S3 업로드용 presigned URL 발급 결과를 표현하는 값 객체입니다.
 *
 * @param key 업로드될 객체의 오브젝트 스토리지 key
 * @param url 클라이언트가 파일을 직접 PUT할 수 있는 presigned URL
 * @param expiresAt [url]의 만료 시각
 */
data class PresignedUpload(
    val key: String,
    val url: String,
    val expiresAt: Instant,
)
