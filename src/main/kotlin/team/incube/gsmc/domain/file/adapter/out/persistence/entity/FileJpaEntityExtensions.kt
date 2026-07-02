package team.incube.gsmc.domain.file.adapter.out.persistence.entity

import team.incube.gsmc.domain.file.File

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
        fileUri = fileUri,
        fileOriginalName = fileOriginalName,
        fileStoredName = fileStoredName,
    )
