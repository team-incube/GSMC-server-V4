@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.sheet.port.`in`

interface FetchGradeScoreSheetUseCase {
    fun execute(grade: Int): String
}
