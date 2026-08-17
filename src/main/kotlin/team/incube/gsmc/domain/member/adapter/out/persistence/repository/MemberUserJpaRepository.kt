package team.incube.gsmc.domain.member.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.member.adapter.out.persistence.MemberPersistenceAdapter
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * 사용자 조회 도메인에서 사용하는 사용자 정보 JPA 저장소 인터페이스입니다.
 * [MemberPersistenceAdapter] 에서 사용됩니다.
 *
 * `auth`/`project`/`score` 도메인에도 각자의 `UserJpaEntity` 저장소가 있어 이름이 겹치면
 * Spring Data JPA 빈 이름 충돌이 발생하므로(#71), 도메인 접두사를 붙여 `MemberUserJpaRepository`로 명명한다.
 */
interface MemberUserJpaRepository : JpaRepository<UserJpaEntity, Long>
