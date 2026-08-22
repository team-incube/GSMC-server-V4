package team.incube.gsmc.domain.file.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.score.ScoreStatus

/**
 * 업로드 파일에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface FileJpaRepository : JpaRepository<FileJpaEntity, Long> {
    fun findAllByFileIdIn(fileIds: Collection<Long>): List<FileJpaEntity>

    /**
     * 오브젝트 스토리지 key로 파일 엔티티를 조회한다.
     *
     * @param fileKey 조회할 오브젝트 스토리지 key
     * @return 해당 파일 엔티티, 없으면 null
     */
    fun findByFileKey(fileKey: String): FileJpaEntity?

    /**
     * 특정 근거 자료에 연결된 모든 파일 엔티티를 조회한다.
     *
     * @param evidenceId 조회할 근거 자료 ID
     * @return 해당 근거 자료에 연결된 파일 엔티티 목록
     */
    fun findAllByEvidenceEvidenceId(evidenceId: Long): List<FileJpaEntity>

    fun findAllByEvidenceEvidenceIdIn(evidenceIds: Collection<Long>): List<FileJpaEntity>

    /**
     * 특정 사용자가 업로드한 모든 파일 엔티티를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 업로드한 파일 엔티티 목록
     */
    fun findAllByUserUserId(userId: Long): List<FileJpaEntity>

    /**
     * 특정 파일이 주어진 상태의 점수 요청에 연결되어 있는지 확인한다.
     *
     * @param fileId 확인할 파일 ID
     * @param scoreStatus 확인할 점수 요청 심사 상태
     * @return 연결되어 있으면 true
     */
    fun existsByFileIdAndScoreScoreStatus(
        fileId: Long,
        scoreStatus: ScoreStatus,
    ): Boolean

    @Modifying
    @Query("update FileJpaEntity f set f.evidence = null where f.evidence.evidenceId = :evidenceId")
    fun unlinkAllFromEvidence(
        @Param("evidenceId") evidenceId: Long,
    ): Int
}
