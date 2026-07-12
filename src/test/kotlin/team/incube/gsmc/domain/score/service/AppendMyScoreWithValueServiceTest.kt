package team.incube.gsmc.domain.score.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class AppendMyScoreWithValueServiceTest :
    BehaviorSpec({
        val appendScoreSupport = mockk<AppendScoreSupport>()
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service =
            AppendMyScoreWithValueService(
                appendScoreSupport = appendScoreSupport,
                scorePersistencePort = scorePersistencePort,
                memberUtil = memberUtil,
            )

        beforeEach { clearAllMocks() }

        val userId = 1L
        val category =
            Category(
                categoryId = 1,
                weight = 1,
                categoryEnglishName = "VOLUNTEER",
                categoryKoreanName = "봉사활동",
                categoryMaximumValue = 10,
                isAccumulated = true,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = CategoryType.VOLUNTEER,
                calculationType = ScoreCalculationType.SCORE_BASED,
            )

        fun freshScore() =
            Score(
                scoreId = 0,
                userId = userId,
                category = category,
                evidence = null,
                file = null,
                scoreStatus = ScoreStatus.PENDING,
                activityName = null,
                scoreValue = null,
                rejectionReason = null,
                dgProjectId = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        Given("증빙 불필요 + SCORE_BASED 카테고리에 제출할 때") {
            When("숫자 값을 입력하면") {
                Then("scoreValue에 파싱된 정수가 저장된다") {
                    every { memberUtil.getCurrentUserId() } returns userId
                    every {
                        appendScoreSupport.resolveUnrequiredCategory(
                            CategoryType.VOLUNTEER,
                            ScoreCalculationType.SCORE_BASED,
                        )
                    } returns category
                    every { appendScoreSupport.parseScoreValue("10") } returns 10
                    every { appendScoreSupport.findOrCreateScore(userId, category) } returns freshScore()
                    every { scorePersistencePort.save(any()) } answers { firstArg<Score>().copy(scoreId = 300L) }

                    val result = service.execute(CategoryType.VOLUNTEER, "10")

                    result.scoreValue shouldBe 10
                    result.activityName shouldBe null
                    result.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }
    })
