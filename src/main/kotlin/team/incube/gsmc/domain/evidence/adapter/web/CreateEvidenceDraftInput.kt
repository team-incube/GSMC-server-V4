package team.incube.gsmc.domain.evidence.adapter.web

data class CreateEvidenceDraftInput(
    val title: String = "",
    val content: String = "",
    val fileIds: List<Long> = emptyList(),
)
