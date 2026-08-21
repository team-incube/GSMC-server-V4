package team.incube.gsmc.domain.alert.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.alert.adapter.out.persistence.entity.AlertJpaEntity

/**
 * 알림에 대한 JPA 기반 저장소 인터페이스입니다. 조회/갱신은 [team.incube.gsmc.domain.alert.adapter.out.persistence.AlertPersistenceAdapter]가
 * QueryDSL로 직접 처리하고, 이 리포지토리는 저장/삭제 용도로만 사용됩니다.
 */
interface AlertJpaRepository : JpaRepository<AlertJpaEntity, Long>
