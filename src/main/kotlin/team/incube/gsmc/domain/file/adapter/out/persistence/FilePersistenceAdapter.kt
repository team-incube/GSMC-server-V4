package team.incube.gsmc.domain.file.adapter.out.persistence

import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.file.adapter.out.persistence.repository.FileJpaRepository
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 업로드 파일 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [FilePersistencePort]를 구현합니다. [FileJpaEntity]의 필드가 전부 불변(`val`)이라, score 연결
 * 변경은 기존 값을 그대로 옮긴 새 엔티티를 만들어 같은 ID로 저장(update)하는 방식으로 처리합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class FilePersistenceAdapter(
    private val fileJpaRepository: FileJpaRepository,
    private val entityManager: EntityManager,
) : FilePersistencePort {
    override fun findById(fileId: Long): File? = fileJpaRepository.findById(fileId).orElse(null)?.toDomain()

    override fun findByScoreId(scoreId: Long): File? = fileJpaRepository.findByScoreScoreId(scoreId)?.toDomain()

    override fun linkToScore(
        fileId: Long,
        scoreId: Long,
    ) {
        val entity = fileJpaRepository.findById(fileId).orElse(null) ?: return
        val score = entityManager.getReference(ScoreJpaEntity::class.java, scoreId)
        fileJpaRepository.save(entity.copy(score = score))
    }

    override fun unlinkFromScore(fileId: Long) {
        val entity = fileJpaRepository.findById(fileId).orElse(null) ?: return
        fileJpaRepository.save(entity.copy(score = null))
    }

    private fun FileJpaEntity.copy(score: ScoreJpaEntity?): FileJpaEntity =
        FileJpaEntity(
            fileId = fileId,
            user = user,
            score = score,
            evidence = evidence,
            fileUri = fileUri,
            fileOriginalName = fileOriginalName,
            fileStoredName = fileStoredName,
        )
}
