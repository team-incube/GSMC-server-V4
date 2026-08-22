package team.incube.gsmc.domain.alert.adapter.out.persistence.entity

import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * [AlertJpaEntity]를 도메인 모델 [Alert]로 변환한다.
 *
 * @receiver 변환할 JPA 엔티티
 * @return 변환된 [Alert] 도메인 객체
 */
fun AlertJpaEntity.toDomain(): Alert =
    Alert(
        alertId = alertId,
        userId = user.userId,
        scoreId = score?.scoreId,
        alertType = alertType,
        content = alertContent,
        isRead = isRead,
        createdAt = createdAt,
    )

/**
 * 도메인 모델 [Alert]를 [AlertJpaEntity]로 변환한다. [user]/[score]는 저장 시점에 FK 컬럼만 채우면
 * 되므로 전체 로드 없이 참조([jakarta.persistence.EntityManager.getReference])만 있어도 된다.
 *
 * @receiver 변환할 도메인 객체
 * @param user 알림을 수신하는 사용자의 엔티티(또는 참조)
 * @param score 연관된 점수 요청의 엔티티(또는 참조), 없으면 null
 * @return 변환된 [AlertJpaEntity] JPA 엔티티
 */
fun Alert.toEntity(
    user: UserJpaEntity,
    score: ScoreJpaEntity?,
): AlertJpaEntity =
    AlertJpaEntity(
        alertId = alertId,
        user = user,
        score = score,
        alertType = alertType,
        alertContent = content,
        isRead = isRead,
    ).apply {
        this.createdAt = this@toEntity.createdAt
    }
