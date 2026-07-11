package team.incube.gsmc.domain.file.port.out

import team.incube.gsmc.domain.file.File

/**
 * 업로드 파일 영속성을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface FilePersistencePort {
    /**
     * ID로 파일을 조회한다.
     *
     * @param fileId 조회할 파일 ID
     * @return 해당 파일, 없으면 null
     */
    fun findById(fileId: Long): File?

    /**
     * 파일을 점수 요청에 직접 연결한다(score FK 갈아끼움). [team.incube.gsmc.domain.category.EvidenceType.FILE]
     * 증빙 카테고리에서 사용한다.
     *
     * @param fileId 연결할 파일 ID
     * @param scoreId 연결할 점수 요청 ID
     */
    fun linkToScore(
        fileId: Long,
        scoreId: Long,
    )

    /**
     * 파일의 점수 요청 연결을 해제한다(score FK를 null로 갈아끼움).
     *
     * @param fileId 연결을 해제할 파일 ID
     */
    fun unlinkFromScore(fileId: Long)
}
