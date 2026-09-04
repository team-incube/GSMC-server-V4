package team.incube.gsmc.domain.file.adapter.out.persistence

import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.toDomain
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.toEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.repository.FileJpaRepository
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/**
 * 업로드 파일 영속성 처리를 담당하는 아웃바운드 어댑터 클래스입니다.
 * [FilePersistencePort]를 구현합니다. [FileJpaEntity]의 필드가 전부 불변(`val`)이라, score/evidence 연결
 * 변경은 기존 값을 그대로 옮긴 새 엔티티를 만들어 같은 ID로 저장(update)하는 방식으로 처리합니다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class FilePersistenceAdapter(
    private val fileJpaRepository: FileJpaRepository,
    private val entityManager: EntityManager,
) : FilePersistencePort {
    override fun findById(fileId: Long): File? = fileJpaRepository.findById(fileId).orElse(null)?.toDomain()

    override fun findAllByIdIn(fileIds: Collection<Long>): List<File> =
        if (fileIds.isEmpty()) emptyList() else fileJpaRepository.findAllByFileIdIn(fileIds).map { it.toDomain() }

    override fun findByFileKey(fileKey: String): File? = fileJpaRepository.findByFileKey(fileKey)?.toDomain()

    override fun findAllByEvidenceId(evidenceId: Long): List<File> =
        fileJpaRepository.findAllByEvidenceEvidenceId(evidenceId).map { it.toDomain() }

    override fun findAllByEvidenceIdIn(evidenceIds: Collection<Long>): List<File> =
        if (evidenceIds.isEmpty()) {
            emptyList()
        } else {
            fileJpaRepository.findAllByEvidenceEvidenceIdIn(evidenceIds).map {
                it.toDomain()
            }
        }

    override fun findAllByUserId(userId: Long): List<File> =
        fileJpaRepository.findAllByUserUserId(userId).map { it.toDomain() }

    override fun save(file: File): File {
        val user = entityManager.getReference(UserJpaEntity::class.java, file.userId)
        return fileJpaRepository.save(file.toEntity(user)).toDomain()
    }

    override fun deleteById(fileId: Long) {
        fileJpaRepository.deleteById(fileId)
    }

    override fun linkToEvidence(
        fileId: Long,
        evidenceId: Long,
    ) {
        val entity = fileJpaRepository.findById(fileId).orElse(null) ?: return
        val evidence = entityManager.getReference(EvidenceJpaEntity::class.java, evidenceId)
        fileJpaRepository.save(entity.copy(evidence = evidence))
    }

    override fun unlinkFromEvidence(fileId: Long) {
        val entity = fileJpaRepository.findById(fileId).orElse(null) ?: return
        fileJpaRepository.save(entity.copy(evidence = null))
    }

    override fun unlinkAllFromEvidence(evidenceId: Long) {
        fileJpaRepository.unlinkAllFromEvidence(evidenceId)
    }

    override fun linkToScore(
        fileId: Long,
        scoreId: Long,
    ) {
        val entity = fileJpaRepository.findById(fileId).orElse(null) ?: return
        val score = entityManager.getReference(ScoreJpaEntity::class.java, scoreId)
        fileJpaRepository.save(entity.copy(score = score))
    }

    override fun unlinkFromScore(fileId: Long) {
        fileJpaRepository.unlinkFromScore(fileId)
    }

    override fun isLinkedToApprovedScore(fileId: Long): Boolean =
        fileJpaRepository.existsByFileIdAndScoreScoreStatus(fileId, ScoreStatus.APPROVED)

    private fun FileJpaEntity.copy(
        score: ScoreJpaEntity? = this.score,
        evidence: EvidenceJpaEntity? = this.evidence,
    ): FileJpaEntity =
        FileJpaEntity(
            fileId = fileId,
            user = user,
            score = score,
            evidence = evidence,
            fileKey = fileKey,
            fileOriginalName = fileOriginalName,
            fileStoredName = fileStoredName,
        )
}
