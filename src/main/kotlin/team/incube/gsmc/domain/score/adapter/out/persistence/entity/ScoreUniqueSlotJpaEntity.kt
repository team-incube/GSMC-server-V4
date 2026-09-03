package team.incube.gsmc.domain.score.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.gsmc.domain.score.ScoreStatus

/**
 * 비누적 카테고리의 점수 요청 중복을 DB 레벨에서 막기 위한 슬롯 엔티티
 *
 * 비누적([team.incube.gsmc.domain.category.Category.isAccumulated]=false) 카테고리는 사용자당
 * "승인된 점수 1건 + 승인 대기 중인 점수 1건"까지만 존재해야 한다. `is_accumulated`가
 * `category_tb`에 있어 `score_tb`의 CHECK로는 표현할 수 없고(CHECK는 같은 행만 참조 가능),
 * MySQL은 부분 유니크 인덱스도 지원하지 않는다. 그래서 제약이 필요한 행만 이 테이블에 담고
 * `(user_id, category_id, slot_kind)` UNIQUE로 중복을 차단한다.
 *
 * 조회 목적이 없는 제약 전용 테이블이라 도메인 모델을 두지 않으며, user/category도 연관관계 대신
 * 식별자로만 보관한다. `score_id`는 `score_tb`와 PK를 공유하고 DDL에 `ON DELETE CASCADE`가
 * 걸려 있어 점수 삭제 시 함께 정리된다.
 *
 * @see ScoreSlotKind
 */
@Entity
@Table(
    name = "score_unique_slot_tb",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_score_unique_slot", columnNames = ["user_id", "category_id", "slot_kind"]),
    ],
)
class ScoreUniqueSlotJpaEntity(
    @Id
    @Column(name = "score_id", nullable = false)
    val scoreId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "category_id", nullable = false)
    val categoryId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_kind", nullable = false, length = 20)
    val slotKind: ScoreSlotKind,
)

/**
 * 슬롯의 종류 — [ScoreStatus] 4종을 심사 완료 여부 2종으로 접은 값
 *
 * 자리를 둘로 나눠야 승인된 점수와 심사 중인 재제출이 공존할 수 있고, 그래야 재심사 중에도
 * 기존 승인 점수가 인정 점수에서 빠지지 않는다.
 */
enum class ScoreSlotKind {
    APPROVED,
    UNAPPROVED,
    ;

    companion object {
        fun of(scoreStatus: ScoreStatus): ScoreSlotKind =
            if (scoreStatus ==
                ScoreStatus.APPROVED
            ) {
                APPROVED
            } else {
                UNAPPROVED
            }
    }
}
