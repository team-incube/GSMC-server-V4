package team.incube.gsmc.domain.score

/**
 * 점수 요청 심사 상태
 *
 * 상태 전이: `INCOMPLETE → PENDING → APPROVED`
 *                               `↘ REJECTED → PENDING (재제출)`
 */
enum class ScoreStatus {
    /** 학생이 아직 제출하지 않은 초기 상태 */
    INCOMPLETE,

    /** 제출 후 교사 검토 대기 중 */
    PENDING,

    /** 교사 승인 완료 — 최종 점수에 반영 */
    APPROVED,

    /** 교사 반려 — 수정 후 재제출 가능 */
    REJECTED,
}
