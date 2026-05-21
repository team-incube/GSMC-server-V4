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
)
