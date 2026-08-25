package team.incube.gsmc.domain.evidence.adapter.web

data class PatchEvidenceInput(
    val scoreId: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val fileIds: List<Long>? = null,
)
