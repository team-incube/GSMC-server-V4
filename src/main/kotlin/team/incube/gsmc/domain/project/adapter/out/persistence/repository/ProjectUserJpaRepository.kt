package team.incube.gsmc.domain.project.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * 프로젝트 도메인에서 사용하는 사용자 정보 JPA 저장소 인터페이스입니다.
 * [team.incube.gsmc.domain.project.adapter.out.persistence.ProjectMemberPersistenceAdapter]에서 사용됩니다.
 */
interface ProjectUserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    /** 식별자 목록에 해당하는 사용자를 조회합니다. */
    fun findAllByUserIdIn(userIds: Collection<Long>): List<UserJpaEntity>
}
