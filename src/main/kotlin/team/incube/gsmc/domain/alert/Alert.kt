package team.incube.gsmc.domain.alert

import java.time.LocalDateTime

/**
 * 알림 도메인 모델
 *
 * 인프라 의존성 없는 순수 도메인 객체로, 서비스 계층의 비즈니스 로직이 이 객체를 통해 알림을 다룬다.
 * [scoreId]는 점수 처리 결과 알림([AlertType.APPROVED]/[AlertType.REJECTED])에서만 값이 있으며,
 * 연관 점수가 없는 알림을 고려해 nullable로 설계한다.
 * DB 연동이 필요한 경우 [team.incube.gsmc.domain.alert.adapter.out.persistence.entity.AlertJpaEntity]로 변환한다.
 *
 * @param alertId 알림 고유 식별자
 * @param userId 알림을 수신하는 사용자 ID
 * @param scoreId 연관된 점수 요청 ID, 없으면 null
 * @param alertType 알림 종류
 * @param content 알림 내용
 * @param isRead 읽음 여부
 * @param createdAt 생성 일시
 * @see AlertType
 */
data class Alert(
    val alertId: Long,
    val userId: Long,
    val scoreId: Long?,
    val alertType: AlertType,
    val content: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        /**
         * 점수 승인 알림을 생성한다.
         *
         * @param userId 점수 소유 학생(수신자) ID
         * @param scoreId 승인된 점수 요청 ID
         * @param categoryName 점수가 속한 카테고리명
         */
        fun approved(
            userId: Long,
            scoreId: Long,
            categoryName: String,
        ): Alert =
            Alert(
                alertId = 0,
                userId = userId,
                scoreId = scoreId,
                alertType = AlertType.APPROVED,
                content = "$categoryName 점수가 승인되었습니다.",
                isRead = false,
                createdAt = LocalDateTime.now(),
            )

        /**
         * 점수 거절 알림을 생성한다. 사용자가 거절 사유를 확인할 수 있도록 [content]에 포함한다.
         *
         * @param userId 점수 소유 학생(수신자) ID
         * @param scoreId 거절된 점수 요청 ID
         * @param categoryName 점수가 속한 카테고리명
         * @param rejectionReason 거절 사유
         */
        fun rejected(
            userId: Long,
            scoreId: Long,
            categoryName: String,
            rejectionReason: String,
        ): Alert =
            Alert(
                alertId = 0,
                userId = userId,
                scoreId = scoreId,
                alertType = AlertType.REJECTED,
                content = "$categoryName 점수가 거절되었습니다. 사유: $rejectionReason",
                isRead = false,
                createdAt = LocalDateTime.now(),
            )
    }
}
