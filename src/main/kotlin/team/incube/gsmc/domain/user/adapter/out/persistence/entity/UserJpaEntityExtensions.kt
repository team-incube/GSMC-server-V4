package team.incube.gsmc.domain.user.adapter.out.persistence.entity

import team.incube.gsmc.domain.user.User

/**
 * [UserJpaEntity]를 도메인 모델 [User]로 변환한다.
 *
 * @receiver 변환할 JPA 엔티티
 * @return 변환된 [User] 도메인 객체
 */
fun UserJpaEntity.toDomain(): User =
    User(
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        userGrade = userGrade,
        userClassNumber = userClassNumber,
        userNumber = userNumber,
        userRole = userRole,
        homeroomGrade = homeroomGrade,
        homeroomClassNumber = homeroomClassNumber,
    )

/**
 * 도메인 모델 [User]를 [UserJpaEntity]로 변환한다.
 *
 * @receiver 변환할 도메인 객체
 * @return 변환된 [UserJpaEntity] JPA 엔티티
 */
fun User.toEntity(): UserJpaEntity =
    UserJpaEntity(
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        userGrade = userGrade,
        userClassNumber = userClassNumber,
        userNumber = userNumber,
        userRole = userRole,
        homeroomGrade = homeroomGrade,
        homeroomClassNumber = homeroomClassNumber,
    )
