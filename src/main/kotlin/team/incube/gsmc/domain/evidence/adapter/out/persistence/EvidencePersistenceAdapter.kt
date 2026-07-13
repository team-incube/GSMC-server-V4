package team.incube.gsmc.domain.evidence.adapter.out.persistence

import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.domain.evidence.adapter.out.persistence.repository.EvidenceJpaRepository
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 근거 자료 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [EvidencePersistencePort]를 구현하며, 조회는 [EvidenceJpaRepository]에, 저장 시 사용자 FK 참조 조립은
 * [EntityManager.getReference]에 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class EvidencePersistenceAdapter(
    private val evidenceJpaRepository: EvidenceJpaRepository,
    private val entityManager: EntityManager,
) : EvidencePersistencePort {
    override fun findById(evidenceId: Long): Evidence? =
        evidenceJpaRepository.findById(evidenceId).orElse(null)?.toDomain()

    override fun save(evidence: Evidence): Evidence {
        val user = entityManager.getReference(UserJpaEntity::class.java, evidence.userId)
        return evidenceJpaRepository.save(evidence.toEntity(user)).toDomain()
    }
}
