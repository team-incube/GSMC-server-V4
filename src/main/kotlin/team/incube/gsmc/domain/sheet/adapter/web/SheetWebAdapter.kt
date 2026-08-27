package team.incube.gsmc.domain.sheet.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.sheet.port.`in`.FetchClassScoreSheetUseCase
import team.incube.gsmc.domain.sheet.port.`in`.FetchGradeScoreSheetUseCase

@Controller
class SheetWebAdapter(
    private val fetchClassScoreSheetUseCase: FetchClassScoreSheetUseCase,
    private val fetchGradeScoreSheetUseCase: FetchGradeScoreSheetUseCase,
) {
    @QueryMapping
    fun classScoreSheetUrl(
        @Argument grade: Int,
        @Argument classNumber: Int,
    ): String = fetchClassScoreSheetUseCase.execute(grade, classNumber)

    @QueryMapping
    fun gradeScoreSheetUrl(
        @Argument grade: Int,
    ): String = fetchGradeScoreSheetUseCase.execute(grade)
}
