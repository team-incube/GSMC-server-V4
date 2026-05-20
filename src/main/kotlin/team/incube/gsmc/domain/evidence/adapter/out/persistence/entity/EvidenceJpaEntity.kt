package team.incube.gsmc.domain.evidence.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import java.time.LocalDateTime

/**
 * 근거 자료 제출 엔티티
 *
 * 학생이 점수 요청 시 첨부하는 근거 자료를 저장한다.
 * 타 엔티티와 달리 BaseTimeEntity 를 상속하지 않고 자체 timestamp 컬럼을 보유한다.
 */
@Entity
@Table(name = "evidence_tb")
@EntityListeners(AuditingEntityListener::class)
class EvidenceJpaEntity(
    /** 근거 자료 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id", nullable = false)
    val evidenceId: Long = 0,
    /** 근거 자료를 제출한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    /** 근거 자료 제목 */
    @Column(name = "evidence_title", nullable = false, length = 255)
    val evidenceTitle: String,
    /** 근거 자료 내용 */
    @Column(name = "evidence_content", nullable = false, length = 255)
    val evidenceContent: String,
) {
    /** 최초 등록 일시 */
    @CreatedDate
    @Column(name = "evidence_created_at", nullable = false, updatable = false)
    lateinit var evidenceCreatedAt: LocalDateTime

    /** 최종 수정 일시 */
    @LastModifiedDate
    @Column(name = "evidence_updated_at", nullable = false)
    lateinit var evidenceUpdatedAt: LocalDateTime
}
