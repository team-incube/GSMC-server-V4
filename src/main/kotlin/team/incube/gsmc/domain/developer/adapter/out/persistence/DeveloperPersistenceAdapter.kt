package team.incube.gsmc.domain.developer.adapter.out.persistence

import team.incube.gsmc.domain.developer.adapter.out.persistence.repository.DeveloperEvidenceJpaRepository
import team.incube.gsmc.domain.developer.adapter.out.persistence.repository.DeveloperFileJpaRepository
import team.incube.gsmc.domain.developer.adapter.out.persistence.repository.DeveloperScoreJpaRepository
import team.incube.gsmc.domain.developer.adapter.out.persistence.repository.DeveloperUserJpaRepository
import team.incube.gsmc.domain.developer.port.out.DeveloperPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 개발자 도메인의 사용자 조회/저장을 담당하는 아웃바운드 어댑터 클래스입니다.
 * [DeveloperPersistencePort]를 구현하며, [DeveloperUserJpaRepository]를 통해 JPA에 실제 처리를 위임합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class DeveloperPersistenceAdapter(
    private val developerUserJpaRepository: DeveloperUserJpaRepository,
    private val developerEvidenceJpaRepository: DeveloperEvidenceJpaRepository,
    private val developerScoreJpaRepository: DeveloperScoreJpaRepository,
    private val developerFileJpaRepository: DeveloperFileJpaRepository,
) : DeveloperPersistencePort {
    override fun findByMemberId(memberId: Long): User? =
        developerUserJpaRepository.findById(memberId).orElse(null)?.toDomain()

    override fun findBySchoolInfo(
        grade: Int,
        classNumber: Int,
        number: Int,
    ): User? =
        developerUserJpaRepository
            .findByUserGradeAndUserClassNumberAndUserNumber(grade, classNumber, number)
            ?.toDomain()

    override fun findByEmail(email: String): User? =
        developerUserJpaRepository
            .findByUserEmail(email)
            ?.toDomain()

    override fun hasRelatedData(memberId: Long): Boolean =
        developerEvidenceJpaRepository.existsByUserUserId(memberId) ||
            developerScoreJpaRepository.existsByUserUserId(memberId) ||
            developerFileJpaRepository.existsByUserUserId(memberId)

    override fun delete(user: User) {
        developerUserJpaRepository.deleteById(user.userId)
    }

    override fun save(user: User): User = developerUserJpaRepository.save(user.toEntity()).toDomain()
}
