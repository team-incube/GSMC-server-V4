package team.incube.gsmc.domain.project.adapter.out.persistence

import team.incube.gsmc.domain.project.adapter.out.persistence.repository.UserJpaRepository
import team.incube.gsmc.domain.project.port.out.ProjectMemberPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 프로젝트 도메인의 사용자 조회를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [ProjectMemberPersistencePort]를 구현하며, 조회를 [UserJpaRepository]에 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class ProjectMemberPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : ProjectMemberPersistencePort {
    override fun findByUserId(userId: Long): User? = userJpaRepository.findById(userId).orElse(null)?.toDomain()
}
