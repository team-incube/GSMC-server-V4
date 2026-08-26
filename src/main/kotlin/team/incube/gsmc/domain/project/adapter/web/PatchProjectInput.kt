package team.incube.gsmc.domain.project.adapter.web

/** Project 부분 수정 GraphQL 입력입니다. null 필드는 기존 값을 유지합니다. */
data class PatchProjectInput(
    val title: String? = null,
    val description: String? = null,
    val fileIds: List<Long>? = null,
    val participantIds: List<Long>? = null,
)
