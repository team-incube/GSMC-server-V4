package team.incube.gsmc.domain.project.adapter.web

/** Project 초안 저장 GraphQL 입력입니다. */
data class CreateProjectDraftInput(
    val title: String = "",
    val description: String = "",
    val fileIds: List<Long> = emptyList(),
    val participantIds: List<Long> = emptyList(),
)
