package team.incube.gsmc.domain.project

/** 내부에서 관리하는 프로젝트 도메인 모델입니다. */
data class Project(
    /** 프로젝트 식별자입니다. */
    val projectId: Long,
    /** 프로젝트 소유자 식별자입니다. */
    val ownerId: Long,
    /** 프로젝트 제목입니다. */
    val title: String,
    /** 프로젝트 설명입니다. */
    val description: String,
    /** 프로젝트 참여자 목록입니다. */
    val participants: List<ProjectParticipant> = emptyList(),
    /** 프로젝트에 연결된 파일 목록입니다. */
    val files: List<ProjectFile> = emptyList(),
    /** 프로젝트에 연결된 점수 식별자 목록입니다. */
    val scoreIds: List<Long> = emptyList(),
) {
    /** 외부 응답에서 사용하는 프로젝트 식별자입니다. */
    val id: Long
        get() = projectId
}

/** 내부 프로젝트에 참여한 사용자 요약입니다. */
data class ProjectParticipant(
    /** 참여자 식별자입니다. */
    val id: Long,
    /** 참여자 이름입니다. */
    val name: String,
)

/** 내부 프로젝트에 연결된 파일 요약입니다. */
data class ProjectFile(
    /** 파일 식별자입니다. */
    val id: Long,
    /** 원본 파일 이름입니다. */
    val name: String,
    /** 저장소에서 사용하는 파일 키입니다. */
    val fileKey: String,
)

/** 프로젝트 목록 조회에 사용하는 요약 모델입니다. */
data class ProjectSummary(
    /** 프로젝트 식별자입니다. */
    val id: Long,
    /** 프로젝트 제목입니다. */
    val title: String,
    /** 프로젝트 소유자 식별자입니다. */
    val ownerId: Long,
)

/** 프로젝트 제목 검색 결과입니다. */
data class ProjectSearchResult(
    /** 검색 조건에 해당하는 전체 프로젝트 수입니다. */
    val totalElements: Long,
    /** 현재 페이지의 프로젝트 요약 목록입니다. */
    val projects: List<ProjectSummary>,
)

/** 사용자별 단일 프로젝트 초안입니다. */
data class ProjectDraft(
    /** 초안 제목입니다. */
    val title: String,
    /** 초안 설명입니다. */
    val description: String,
    /** 초안에 포함할 참여자 식별자 목록입니다. */
    val participantIds: List<Long>,
    /** 초안에 연결할 파일 식별자 목록입니다. */
    val fileIds: List<Long>,
)

/** 현재 사용자의 프로젝트 점수와 증빙 자료입니다. */
data class ProjectScoreAndEvidence(
    /** 프로젝트에 연결된 현재 사용자의 점수입니다. */
    val score: team.incube.gsmc.domain.score.Score,
    /** 점수에 연결된 증빙자료 목록입니다. */
    val evidence: List<team.incube.gsmc.domain.evidence.Evidence>,
)
