package team.incube.gsmc.domain.evidence.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity

/**
 * 근거 자료에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface EvidenceJpaRepository : JpaRepository<EvidenceJpaEntity, Long>
