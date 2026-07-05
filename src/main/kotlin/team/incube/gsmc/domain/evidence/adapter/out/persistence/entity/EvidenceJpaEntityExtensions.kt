package team.incube.gsmc.domain.evidence.adapter.out.persistence.entity

import team.incube.gsmc.domain.evidence.Evidence

/**
 * [EvidenceJpaEntity]를 도메인 모델 [Evidence]로 변환한다.
 *
 * @receiver 변환할 JPA 엔티티
 * @return 변환된 [Evidence] 도메인 객체
 */
fun EvidenceJpaEntity.toDomain(): Evidence =
    Evidence(
        evidenceId = evidenceId,
        userId = user.userId,
        evidenceTitle = evidenceTitle,
        evidenceContent = evidenceContent,
        evidenceCreatedAt = evidenceCreatedAt,
        evidenceUpdatedAt = evidenceUpdatedAt,
    )
