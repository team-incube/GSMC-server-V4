package team.incube.gsmc.domain.score.adapter.web

data class SubmitProjectParticipationInput(
    val content: String,
    val fileIds: List<Long>,
)
