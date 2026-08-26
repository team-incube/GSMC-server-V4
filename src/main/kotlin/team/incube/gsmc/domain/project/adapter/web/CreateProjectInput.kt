package team.incube.gsmc.domain.project.adapter.web

/** Project 생성 GraphQL 입력입니다. */
data class CreateProjectInput(
    val title: String,
    val description: String,
    val fileIds: List<Long> = emptyList(),
    val participantIds: List<Long>,
)
