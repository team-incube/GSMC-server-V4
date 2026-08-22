package team.incube.gsmc.domain.alert

/**
 * 알림 종류
 *
 * | 값         | 발생 시점                     |
 * |------------|--------------------------------|
 * | ADD_SCORE  | 점수 요청 제출 (수신자 정책 미확정) |
 * | APPROVED   | 점수 승인                      |
 * | REJECTED   | 점수 거절                      |
 */
enum class AlertType {
    /** 점수 요청 제출 — 현재 알림 생성 로직에서는 사용하지 않는다 */
    ADD_SCORE,

    /** 점수 승인 */
    APPROVED,

    /** 점수 거절 */
    REJECTED,
}
