package team.incube.gsmc.domain.score.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreUniqueSlotJpaEntity

/**
 * 비누적 카테고리 중복 방지 슬롯([ScoreUniqueSlotJpaEntity]) 저장소 인터페이스입니다.
 * 슬롯 점유는 유니크 위반을 즉시 감지해야 하므로 `saveAndFlush`로 수행합니다. 갱신·삭제는
 * [team.incube.gsmc.domain.score.adapter.out.persistence.ScorePersistenceAdapter]가 QueryDSL로 직접 처리합니다.
 */
interface ScoreUniqueSlotJpaRepository : JpaRepository<ScoreUniqueSlotJpaEntity, Long>
