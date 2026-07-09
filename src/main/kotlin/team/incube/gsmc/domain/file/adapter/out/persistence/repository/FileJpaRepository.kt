package team.incube.gsmc.domain.file.adapter.out.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity

/**
 * 업로드 파일에 대한 JPA 기반 저장소 인터페이스입니다.
 */
interface FileJpaRepository : JpaRepository<FileJpaEntity, Long>
