package team.incube.gsmc.domain.developer.port.out

import team.incube.gsmc.domain.user.User

/**
 * 개발자 도메인에서 필요한 사용자(회원) 조회·저장을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface DeveloperPersistencePort {
    /**
     * 사용자 ID로 사용자를 조회한다.
     *
     * @param memberId 조회할 사용자ID
     * @return 해당 사용자, 없으면 null
     */
    fun findByMemberId(memberId: Long): User?

    /**
     * 학년·반·번호 조합으로 사용자를 조회한다. 학적정보 변경 시 다른 사용자와의 중복 여부를
     * 확인하는 데 사용한다.
     *
     * @param grade 조회할 학년
     * @param classNumber 조회할 반 번호
     * @param number 조회할 번호
     * @return 해당 학적정보를 가진 사용자, 없으면 null
     */
    fun findBySchoolInfo(
        grade: Int,
        classNumber: Int,
        number: Int,
    ): User?

    /**
     * 사용자를 저장한다. [user]의 userId가 기존 사용자의 ID와 같으면 값을 갈아끼우고(update),
     * 0이면 새로 생성한다(insert).
     *
     * @param user 저장할 사용자 도메인 객체
     * @return 저장된 사용자 도메인 객체
     */
    fun save(user: User): User
}
