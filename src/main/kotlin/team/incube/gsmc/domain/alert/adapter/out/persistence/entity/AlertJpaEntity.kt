package team.incube.gsmc.domain.alert.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import team.incube.gsmc.domain.alert.AlertType
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import java.time.LocalDateTime

/**
 * 알림 엔티티
 *
 * 점수 처리 결과 등 사용자가 확인해야 하는 알림을 저장한다.
 * [score]는 연관 점수가 없는 알림을 고려해 nullable이며, 점수 삭제 시 FK 제약으로 삭제가 실패하지
 * 않도록 삭제 전에 연결을 해제한다.
 *
 * @see AlertType
 */
@Entity
@Table(name = "alert_tb")
@EntityListeners(AuditingEntityListener::class)
class AlertJpaEntity(
    /** 알림 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id", nullable = false)
    val alertId: Long = 0,
    /** 알림을 수신하는 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    /** 연관된 점수 요청 (없을 수 있음) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id", nullable = true)
    val score: ScoreJpaEntity?,
    /** 알림 종류 */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 20)
    val alertType: AlertType,
    /** 알림 내용 */
    @Column(name = "alert_content", nullable = false, columnDefinition = "TEXT")
    val alertContent: String,
    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,
) {
    /** 생성 일시 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}
