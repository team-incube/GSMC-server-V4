package team.incube.gsmc.domain.sheet

data class ScoreSheetRow(
    val grade: Int,
    val classNumber: Int,
    val number: Int,
    val name: String,
    val totalScore: Int,
)
