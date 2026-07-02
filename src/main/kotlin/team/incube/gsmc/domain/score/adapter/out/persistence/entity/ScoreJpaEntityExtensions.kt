package team.incube.gsmc.domain.score.adapter.out.persistence.entity

import team.incube.gsmc.domain.category.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.score.Score

/**
 * [ScoreJpaEntity]를 도메인 모델 [Score]로 변환한다.
 * [file]은 [ScoreJpaEntity]가 직접 참조를 갖고 있지 않아(역방향 연관관계) 호출부에서 별도로 조회해 전달한다.
 *
 * @receiver 변환할 JPA 엔티티
 * @param file 이 점수 요청에 직접 첨부된 파일, 없으면 null
 * @return 변환된 [Score] 도메인 객체
 */
fun ScoreJpaEntity.toDomain(file: File?): Score =
    Score(
        scoreId = scoreId,
        userId = user.userId,
        category = category.toDomain(),
        evidence = evidence?.toDomain(),
        file = file,
        scoreStatus = scoreStatus,
        activityName = activityName,
        scoreValue = scoreValue,
        rejectionReason = rejectionReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
