package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.user.User

/**
 * 프로젝트 도메인에서 필요한 사용자(회원) 조회를 추상화하는 아웃바운드 포트 인터페이스입니다.
 * DataGSM 조회에 필요한 현재 사용자의 이메일 확인에 사용된다.
 */
interface ProjectMemberPersistencePort {
    /**
     * ID로 사용자를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자, 없으면 null
     */
    fun findByUserId(userId: Long): User?

    fun findAllByUserIds(userIds: Collection<Long>): List<User>
}
