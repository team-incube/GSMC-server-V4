package team.incube.gsmc.domain.score.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity

/**
 * 점수 요청에 대한 JPA 기반 저장소 인터페이스입니다. 조회는 [team.incube.gsmc.domain.score.adapter.out.persistence.ScorePersistenceAdapter]가
 * QueryDSL로 직접 처리하고, 이 리포지토리는 저장/삭제 용도로만 사용됩니다.
 */
interface ScoreJpaRepository : JpaRepository<ScoreJpaEntity, Long> {
    @Modifying
    @Query("update ScoreJpaEntity s set s.evidence = null where s.evidence.evidenceId = :evidenceId")
    fun unlinkEvidence(
        @Param("evidenceId") evidenceId: Long,
    ): Int

    @Modifying
    @Query("update ScoreJpaEntity s set s.project = null where s.project.projectId = :projectId")
    fun unlinkProject(
        @Param("projectId") projectId: Long,
    ): Int
}
