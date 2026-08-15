package team.incube.gsmc.domain.file.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * 업로드 파일 엔티티
 *
 * 학생이 점수 요청 또는 근거 자료 제출 시 첨부한 파일 정보를 저장한다.
 * 업로드 직후에는 [score]/[evidence]가 모두 null인 미연결 상태로 존재할 수 있으며,
 * 이후 점수 요청 또는 근거 자료 생성 시 연결된다.
 */
@Entity
@Table(name = "file_tb")
class FileJpaEntity(
    /** 파일 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    val fileId: Long = 0,
    /** 파일을 업로드한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserJpaEntity,
    /** 연결된 점수 요청 (없을 수 있음) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id", nullable = true)
    val score: ScoreJpaEntity?,
    /** 연결된 근거 자료 (없을 수 있음) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", nullable = true)
    val evidence: EvidenceJpaEntity?,
    /** 오브젝트 스토리지(S3) 객체 key */
    @Column(name = "file_uri", nullable = false, unique = true, length = 255)
    val fileKey: String,
    /** 사용자가 업로드한 원본 파일명 */
    @Column(name = "file_original_name", nullable = false, length = 255)
    val fileOriginalName: String,
    /** 서버에 저장된 파일명 */
    @Column(name = "file_stored_name", nullable = false, length = 255)
    val fileStoredName: String,
)
