package team.incube.gsmc.domain.developer.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.developer.adapter.out.persistence.DeveloperPersistenceAdapter
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * 개발자 도메인에서 사용하는 사용자 정보 JPA 저장소 인터페이스입니다.
 * [DeveloperPersistenceAdapter] 에서 사용됩니다.
 *
 * `auth`/`member`/`project`/`score` 도메인에도 각자의 `UserJpaEntity` 저장소가 있어 이름이 겹치면
 * Spring Data JPA 빈 이름 충돌이 발생하므로(#71), 도메인 접두사를 붙여 `DeveloperUserJpaRepository`로 명명한다.
 */
interface DeveloperUserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    /**
     * 학년·반·번호 조합으로 사용자 엔티티를 조회한다.
     * [team.incube.gsmc.domain.developer.adapter.out.persistence.DeveloperPersistenceAdapter]의
     * 학적정보 중복 확인에 사용된다.
     *
     * @param userGrade 조회할 학년
     * @param userClassNumber 조회할 반 번호
     * @param userNumber 조회할 번호
     * @return 해당 학적정보를 가진 사용자 엔티티, 없으면 null
     */
    fun findByUserGradeAndUserClassNumberAndUserNumber(
        userGrade: Int,
        userClassNumber: Int,
        userNumber: Int,
    ): UserJpaEntity?

    /**
     * 이메일로 사용자 엔티티를 조회한다.
     *
     * @param userEmail 조회할 이메일
     * @return 해당 이메일을 가진 사용자 엔티티, 없으면 null
     */
    fun findByUserEmail(userEmail: String): UserJpaEntity?
}
