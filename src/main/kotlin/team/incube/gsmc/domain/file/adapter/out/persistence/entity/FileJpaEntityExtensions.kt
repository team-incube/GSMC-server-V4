package team.incube.gsmc.domain.file.adapter.out.persistence.entity

import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/**
 * [FileJpaEntity]를 도메인 모델 [File]로 변환한다.
 *
 * @receiver 변환할 JPA 엔티티
 * @return 변환된 [File] 도메인 객체
 */
fun FileJpaEntity.toDomain(): File =
    File(
        fileId = fileId,
        userId = user.userId,
        fileKey = fileKey,
        fileOriginalName = fileOriginalName,
        fileStoredName = fileStoredName,
        scoreId = score?.scoreId,
        evidenceId = evidence?.evidenceId,
    )

/**
 * 도메인 모델 [File]을 신규 [FileJpaEntity]로 변환한다. `score`/`evidence`는 아직 어떤 요청과도
 * 연결되지 않은 상태(미연결)로 생성되며, 이후 별도 로직에서 연결된다.
 * [user]는 저장 시점에 FK 컬럼만 채우면 되므로 전체 로드 없이 참조([jakarta.persistence.EntityManager.getReference])만
 * 있어도 된다.
 *
 * @receiver 변환할 도메인 객체
 * @param user 파일을 업로드한 사용자의 엔티티(또는 참조)
 * @return 변환된 [FileJpaEntity] JPA 엔티티
 */
fun File.toEntity(user: UserJpaEntity): FileJpaEntity =
    FileJpaEntity(
        fileId = fileId,
        user = user,
        score = null,
        evidence = null,
        fileKey = fileKey,
        fileOriginalName = fileOriginalName,
        fileStoredName = fileStoredName,
    )
