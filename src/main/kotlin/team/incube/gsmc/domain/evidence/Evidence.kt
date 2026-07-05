package team.incube.gsmc.domain.evidence

import java.time.LocalDateTime

/**
 * 근거 자료 도메인 모델
 *
 * 인프라 의존성 없는 순수 도메인 객체로, 서비스 계층의 비즈니스 로직이 이 객체를 통해 근거 자료를 다룬다.
 * DB 연동이 필요한 경우 [team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity]로 변환한다.
 *
 * @param evidenceId 근거 자료 고유 식별자
 * @param userId 근거 자료를 제출한 사용자 ID
 * @param evidenceTitle 근거 자료 제목
 * @param evidenceContent 근거 자료 내용
 * @param evidenceCreatedAt 최초 등록 일시
 * @param evidenceUpdatedAt 최종 수정 일시
 */
data class Evidence(
    val evidenceId: Long,
    val userId: Long,
    val evidenceTitle: String,
    val evidenceContent: String,
    val evidenceCreatedAt: LocalDateTime?,
    val evidenceUpdatedAt: LocalDateTime?,
)
