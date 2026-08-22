package team.incube.gsmc.domain.evidence.adapter.web

data class CreateEvidenceInput(
    val scoreId: Long,
    val title: String,
    val content: String,
    val fileIds: List<Long> = emptyList(),
)
