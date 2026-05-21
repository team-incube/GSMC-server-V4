package team.incube.gsmc.domain.auth.adapter.out.persistence

import team.incube.gsmc.domain.auth.adapter.out.persistence.repository.UserJpaRepository
import team.incube.gsmc.domain.auth.port.out.UserPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 인증 도메인의 사용자 정보에 대한 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [UserPersistencePort]를 구현하며, 사용자 조회 및 저장 기능을 [UserJpaRepository]를 통해 JPA에 위임합니다.
 * 도메인 ↔ 엔티티 변환은 toDomain/toEntity 확장 함수를 통해 수행합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class AuthUserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserPersistencePort {
    /**
     * 이메일로 사용자를 조회한다.
     *
     * @param email 조회할 이메일
     * @return 해당 이메일의 사용자, 없으면 null
     */
    override fun findByEmail(email: String): User? = userJpaRepository.findByUserEmail(email)?.toDomain()

    /**
     * ID로 사용자를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 ID의 사용자, 없으면 null
     */
    override fun findByUserId(userId: Long): User? = userJpaRepository.findById(userId).orElse(null)?.toDomain()

    /**
     * 사용자를 저장하고 저장된 도메인 객체를 반환한다.
     *
     * @param user 저장할 사용자 도메인 객체
     * @return 저장된 사용자 도메인 객체
     */
    override fun save(user: User): User = userJpaRepository.save(user.toEntity()).toDomain()
}
