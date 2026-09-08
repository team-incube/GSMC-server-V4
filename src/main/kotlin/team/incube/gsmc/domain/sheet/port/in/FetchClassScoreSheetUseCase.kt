@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.sheet.port.`in`

interface FetchClassScoreSheetUseCase {
    fun execute(
        grade: Int,
        classNumber: Int,
    ): String
}
