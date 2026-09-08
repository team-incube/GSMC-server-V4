package team.incube.gsmc.domain.developer.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity

/**
 * 개발자 도메인에서 사용하는 파일 JPA 저장소 인터페이스입니다.
 * 회원 삭제 시 참조 데이터 존재 여부 확인에만 사용됩니다.
 *
 * `file` 도메인에도 자체 저장소가 있으나 Spring Data JPA 빈 이름 충돌을 피하기 위해(#71)
 * 도메인 접두사를 붙여 명명한다.
 */
interface DeveloperFileJpaRepository : JpaRepository<FileJpaEntity, Long> {
    /**
     * 해당 사용자의 파일이 존재하는지 확인한다.
     *
     * @param userId 확인할 사용자 ID
     * @return 하나라도 있으면 true
     */
    fun existsByUserUserId(userId: Long): Boolean
}
