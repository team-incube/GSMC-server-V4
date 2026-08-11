package team.incube.gsmc.domain.file

/**
 * 업로드 파일 도메인 모델
 *
 * 인프라 의존성 없는 순수 도메인 객체로, 서비스 계층의 비즈니스 로직이 이 객체를 통해 파일을 다룬다.
 * DB 연동이 필요한 경우 [team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity]로 변환한다.
 *
 * @param fileId 파일 고유 식별자
 * @param userId 파일을 업로드한 사용자 ID
 * @param fileKey 파일이 저장된 S3 객체 key. 실제 접근 가능한 URL이 아니며,
 * GraphQL 응답 시점에 presigned GET URL(`File.fileUrl`)로 변환된다.
 * @param fileOriginalName 사용자가 업로드한 원본 파일명
 * @param fileStoredName 서버에 저장된 파일명
 */
data class File(
    val fileId: Long,
    val userId: Long,
    val fileKey: String,
    val fileOriginalName: String,
    val fileStoredName: String,
)
