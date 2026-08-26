package team.incube.gsmc.domain.project

/** 내부에서 관리하는 프로젝트 도메인 모델입니다. */
data class Project(
    val projectId: Long,
    val ownerId: Long,
    val title: String,
    val description: String,
    val participants: List<ProjectParticipant> = emptyList(),
    val files: List<ProjectFile> = emptyList(),
    val scoreIds: List<Long> = emptyList(),
) {
    val id: Long
        get() = projectId
}

/** 내부 프로젝트에 참여한 사용자 요약입니다. */
data class ProjectParticipant(
    val id: Long,
    val name: String,
)

/** 내부 프로젝트에 연결된 파일 요약입니다. */
data class ProjectFile(
    val id: Long,
    val name: String,
    val fileKey: String,
)

/** 프로젝트 목록 조회에 사용하는 요약 모델입니다. */
data class ProjectSummary(
    val id: Long,
    val title: String,
    val ownerId: Long,
)

/** 프로젝트 제목 검색 결과입니다. */
data class ProjectSearchResult(
    val totalElements: Long,
    val projects: List<ProjectSummary>,
)

/** 사용자별 단일 프로젝트 초안입니다. */
data class ProjectDraft(
    val title: String,
    val description: String,
    val participantIds: List<Long>,
    val fileIds: List<Long>,
)

/** 현재 사용자의 프로젝트 점수와 증빙 자료입니다. */
data class ProjectScoreAndEvidence(
    val score: team.incube.gsmc.domain.score.Score,
    val evidence: List<team.incube.gsmc.domain.evidence.Evidence>,
)
