package team.incube.gsmc.domain.user

/**
 * 사용자 도메인 모델
 *
 * 인프라 의존성 없는 순수 도메인 객체로, 서비스 계층의 비즈니스 로직이 이 객체를 통해 사용자를 다룬다.
 * DB 연동이 필요한 경우 [team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity]로 변환한다.
 *
 * @param userId 사용자 고유 식별자
 * @param userName 사용자 이름
 * @param userEmail 학교 이메일 (gsm.hs.kr 도메인)
 * @param userGrade 학년 (1~3), 교사는 null
 * @param userClassNumber 반 번호, 교사는 null
 * @param userNumber 번호, 교사는 null
 * @param userRole 권한 역할
 * @param homeroomGrade 담임을 맡은 학년, [UserRole.HOMEROOM_TEACHER]가 아니면 null
 * @param homeroomClassNumber 담임을 맡은 반 번호, [UserRole.HOMEROOM_TEACHER]가 아니면 null
 * @see UserRole
 */
data class User(
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val userGrade: Int?,
    val userClassNumber: Int?,
    val userNumber: Int?,
    val userRole: UserRole,
    val homeroomGrade: Int?,
    val homeroomClassNumber: Int?,
)

/**
 * 다른 사용자([target])의 점수를 조회할 권한이 있는지 확인한다.
 * [UserRole.TEACHER], [UserRole.ROOT]는 전체 학생에 접근 가능하고, [UserRole.HOMEROOM_TEACHER]는
 * 본인이 담임을 맡은 학급([User.homeroomGrade]/[User.homeroomClassNumber])의 학생만 접근 가능하다.
 *
 * @receiver 조회를 시도하는 사용자
 * @param target 조회 대상 사용자
 * @return 접근 가능 여부
 */
fun User.canAccessScoresOf(target: User): Boolean =
    when (userRole) {
        UserRole.TEACHER, UserRole.ROOT -> true
        UserRole.HOMEROOM_TEACHER -> homeroomGrade == target.userGrade && homeroomClassNumber == target.userClassNumber
        UserRole.STUDENT, UserRole.UNAUTHORIZED -> false
    }
