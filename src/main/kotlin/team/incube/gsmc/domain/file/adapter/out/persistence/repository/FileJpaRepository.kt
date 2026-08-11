package team.incube.gsmc.domain.file.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity

/**
 * 업로드 파일에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface FileJpaRepository : JpaRepository<FileJpaEntity, Long> {
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

    /**
     * 특정 사용자가 업로드한 모든 파일 엔티티를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 업로드한 파일 엔티티 목록
     */
    fun findAllByUserUserId(userId: Long): List<FileJpaEntity>
}
