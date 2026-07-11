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

class AppendMyScoreOnlyServiceTest :
    BehaviorSpec({
        val appendScoreSupport = mockk<AppendScoreSupport>()
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service =
            AppendMyScoreOnlyService(
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
                categoryEnglishName = "TOEIC_ACADEMY",
                categoryKoreanName = "토익사관학교",
                categoryMaximumValue = 1,
                isAccumulated = false,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = CategoryType.TOEIC_ACADEMY,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        Given("증빙 불필요 + COUNT_BASED 카테고리에 제출할 때") {
            When("카테고리 유형만으로 신청하면") {
                Then("값 없이 PENDING 상태의 점수가 생성된다") {
                    val fresh =
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
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now(),
                        )
                    every { memberUtil.getCurrentUserId() } returns userId
                    every {
                        appendScoreSupport.resolveUnrequiredCategory(
                            CategoryType.TOEIC_ACADEMY,
                            ScoreCalculationType.COUNT_BASED,
                        )
                    } returns category
                    every { appendScoreSupport.findOrCreateScore(userId, category) } returns fresh
                    every { scorePersistencePort.save(any()) } answers { firstArg<Score>().copy(scoreId = 400L) }

                    val result = service.execute(CategoryType.TOEIC_ACADEMY)

                    result.scoreId shouldBe 400L
                    result.activityName shouldBe null
                    result.scoreValue shouldBe null
                    result.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }
    })
