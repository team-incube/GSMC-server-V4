package team.incube.gsmc.domain.file.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity

/**
 * 업로드 파일에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface FileJpaRepository : JpaRepository<FileJpaEntity, Long> {
    /**
     * 특정 점수 요청에 직접 첨부된 파일 엔티티를 조회한다.
     *
     * @param scoreId 조회할 점수 요청 ID
     * @return 해당 점수 요청에 첨부된 파일 엔티티, 없으면 null
     */
    fun findByScoreScoreId(scoreId: Long): FileJpaEntity?
}
