package team.incube.gsmc.domain.file.port.out

import team.incube.gsmc.domain.file.PresignedUpload

/**
 * 오브젝트 스토리지(S3) 연동을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface FileStoragePort {
    /**
     * 업로드용 presigned URL을 발급한다. 클라이언트는 이 URL로 오브젝트 스토리지에 직접 PUT한다.
     *
     * @param key 업로드될 객체의 key
     * @param contentType 업로드될 파일의 MIME 타입
     * @return presigned URL과 만료 시각
     */
    fun createPresignedUploadUrl(
        key: String,
        contentType: String,
    ): PresignedUpload

    /**
     * 다운로드(조회)용 presigned URL을 발급한다. 호출할 때마다 새로 발급된다.
     *
     * @param key 조회할 객체의 key
     * @return presigned URL
     */
    fun createPresignedDownloadUrl(key: String): String

    /**
     * 객체의 크기를 조회한다.
     *
     * @param key 조회할 객체의 key
     * @return 객체 크기(byte), 해당 key의 객체가 없으면 null
     */
    fun getObjectSize(key: String): Long?

    /**
     * 객체를 삭제한다.
     *
     * @param key 삭제할 객체의 key
     */
    fun deleteObject(key: String)
}
