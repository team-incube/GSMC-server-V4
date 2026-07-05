package team.incube.gsmc.domain.score.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Percentile
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreCategoryGroup
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.TotalScore
import team.incube.gsmc.domain.score.port.`in`.FetchMyPercentInClassUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchMyPercentInGradeUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchMyScoresByCategoryUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchMyScoresUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchMyTotalScoreUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchScoreUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchScoresByCategoryUseCase
import team.incube.gsmc.domain.score.port.`in`.FetchTotalScoreUseCase

/**
 * 점수 조회 GraphQL Query 리졸버입니다.
 * 각 Query를 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class ScoreWebAdapter(
    private val fetchMyScoresUseCase: FetchMyScoresUseCase,
    private val fetchScoreUseCase: FetchScoreUseCase,
    private val fetchMyScoresByCategoryUseCase: FetchMyScoresByCategoryUseCase,
    private val fetchScoresByCategoryUseCase: FetchScoresByCategoryUseCase,
    private val fetchMyTotalScoreUseCase: FetchMyTotalScoreUseCase,
    private val fetchTotalScoreUseCase: FetchTotalScoreUseCase,
    private val fetchMyPercentInClassUseCase: FetchMyPercentInClassUseCase,
    private val fetchMyPercentInGradeUseCase: FetchMyPercentInGradeUseCase,
) {
    @QueryMapping
    fun myScores(
        @Argument categoryType: CategoryType?,
        @Argument status: ScoreStatus?,
    ): List<Score> = fetchMyScoresUseCase.execute(categoryType, status)

    @QueryMapping
    fun score(
        @Argument scoreId: Long,
    ): Score = fetchScoreUseCase.execute(scoreId)

    @QueryMapping
    fun myScoresByCategory(
        @Argument status: ScoreStatus?,
    ): List<ScoreCategoryGroup> = fetchMyScoresByCategoryUseCase.execute(status)

    @QueryMapping
    fun scoresByCategory(
        @Argument memberId: Long,
        @Argument status: ScoreStatus?,
    ): List<ScoreCategoryGroup> = fetchScoresByCategoryUseCase.execute(memberId, status)

    @QueryMapping
    fun myTotalScore(
        @Argument includeApprovedOnly: Boolean?,
    ): TotalScore = fetchMyTotalScoreUseCase.execute(includeApprovedOnly ?: true)

    @QueryMapping
    fun totalScore(
        @Argument memberId: Long,
        @Argument includeApprovedOnly: Boolean?,
    ): TotalScore = fetchTotalScoreUseCase.execute(memberId, includeApprovedOnly ?: true)

    @QueryMapping
    fun myPercentInClass(
        @Argument includeApprovedOnly: Boolean?,
    ): Percentile = fetchMyPercentInClassUseCase.execute(includeApprovedOnly ?: true)

    @QueryMapping
    fun myPercentInGrade(
        @Argument includeApprovedOnly: Boolean?,
    ): Percentile = fetchMyPercentInGradeUseCase.execute(includeApprovedOnly ?: true)
}
