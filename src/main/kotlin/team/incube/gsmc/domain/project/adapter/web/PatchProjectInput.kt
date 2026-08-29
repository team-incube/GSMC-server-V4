package team.incube.gsmc.domain.project.adapter.web

/** Project 부분 수정 GraphQL 입력입니다. null 필드는 기존 값을 유지합니다. */
data class PatchProjectInput(
    /** 변경할 프로젝트 제목입니다. */
    val title: String? = null,
    /** 변경할 프로젝트 설명입니다. */
    val description: String? = null,
    /** 명시하면 파일 연결을 이 목록으로 교체합니다. */
    val fileIds: List<Long>? = null,
    /** 명시하면 참여자 연결을 이 목록으로 교체합니다. */
    val participantIds: List<Long>? = null,
)
