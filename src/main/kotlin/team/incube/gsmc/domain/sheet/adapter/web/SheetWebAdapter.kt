package team.incube.gsmc.domain.sheet.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.sheet.port.`in`.FetchClassScoreSheetUseCase
import team.incube.gsmc.domain.sheet.port.`in`.FetchGradeScoreSheetUseCase

/** 점수 현황 파일 다운로드 URL 조회 GraphQL 요청을 유스케이스에 위임하는 어댑터입니다. */
@Controller
class SheetWebAdapter(
    private val fetchClassScoreSheetUseCase: FetchClassScoreSheetUseCase,
    private val fetchGradeScoreSheetUseCase: FetchGradeScoreSheetUseCase,
) {
    /** 지정한 학년과 반의 점수 현황 파일 다운로드 URL을 조회합니다. */
    @QueryMapping
    fun classScoreSheetUrl(
        @Argument grade: Int,
        @Argument classNumber: Int,
    ): String = fetchClassScoreSheetUseCase.execute(grade, classNumber)

    /** 지정한 학년 전체의 점수 현황 파일 다운로드 URL을 조회합니다. */
    @QueryMapping
    fun gradeScoreSheetUrl(
        @Argument grade: Int,
    ): String = fetchGradeScoreSheetUseCase.execute(grade)
}
