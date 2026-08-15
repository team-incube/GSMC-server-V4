package team.incube.gsmc.domain.score.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * 점수 도메인에서 사용하는 사용자 정보 JPA 저장소 인터페이스입니다.
 * [MemberPersistenceAdapter][team.incube.gsmc.domain.score.adapter.out.persistence.MemberPersistenceAdapter]에서 사용됩니다.
 */
interface ScoreUserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    /**
     * 특정 역할, 학년의 사용자를 조회한다.
     *
     * @param userRole 조회할 역할
     * @param userGrade 조회할 학년
     * @return 해당 조건의 사용자 목록
     */
    fun findAllByUserRoleAndUserGrade(
        userRole: UserRole,
        userGrade: Int,
    ): List<UserJpaEntity>

    /**
     * 특정 역할, 학년, 반의 사용자를 조회한다.
     *
     * @param userRole 조회할 역할
     * @param userGrade 조회할 학년
     * @param userClassNumber 조회할 반 번호
     * @return 해당 조건의 사용자 목록
     */
    fun findAllByUserRoleAndUserGradeAndUserClassNumber(
        userRole: UserRole,
        userGrade: Int,
        userClassNumber: Int,
    ): List<UserJpaEntity>
}
