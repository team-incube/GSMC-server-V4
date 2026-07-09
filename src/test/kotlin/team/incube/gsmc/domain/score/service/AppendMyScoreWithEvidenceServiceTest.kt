package team.incube.gsmc.domain.score.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class AppendMyScoreWithEvidenceServiceTest :
    BehaviorSpec({
        val appendScoreSupport = mockk<AppendScoreSupport>()
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service =
            AppendMyScoreWithEvidenceService(
                appendScoreSupport = appendScoreSupport,
                scorePersistencePort = scorePersistencePort,
                memberUtil = memberUtil,
            )

        beforeEach { clearAllMocks() }

        val userId = 1L

        fun category(categoryType: CategoryType) =
            Category(
                categoryId = 1,
                weight = 1,
                categoryEnglishName = "AWARD",
                categoryKoreanName = "수상경력",
                categoryMaximumValue = 10,
                isAccumulated = true,
                evidenceType = EvidenceType.EVIDENCE,
                categoryType = categoryType,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        fun freshScore(category: Category) =
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

        Given("EVIDENCE 증빙 카테고리에 제출할 때") {
            When("활동 내용을 입력하면") {
                Then("activityName에 저장된다") {
                    val cat = category(CategoryType.AWARD)
                    every { memberUtil.getCurrentUserId() } returns userId
                    every { appendScoreSupport.resolveCategory(CategoryType.AWARD, EvidenceType.EVIDENCE) } returns cat
                    every { appendScoreSupport.findOrCreateScore(userId, cat) } returns freshScore(cat)
                    every { scorePersistencePort.save(any()) } answers { firstArg<Score>().copy(scoreId = 200L) }

                    val result = service.execute(CategoryType.AWARD, "전국 SW 해커톤 1위")

                    result.activityName shouldBe "전국 SW 해커톤 1위"
                    result.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }

        Given("categoryType이 PROJECT_PARTICIPATION일 때") {
            When("이 뮤테이션으로 제출을 시도하면") {
                Then("INVALID_CATEGORY_TYPE 예외가 발생하고 카테고리 조회조차 하지 않는다") {
                    every { memberUtil.getCurrentUserId() } returns userId

                    val exception =
                        shouldThrow<GsmcException> {
                            service.execute(CategoryType.PROJECT_PARTICIPATION, "내용")
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_CATEGORY_TYPE
                    verify(exactly = 0) { appendScoreSupport.resolveCategory(any(), any()) }
                }
            }
        }

        Given("REJECTED 상태의 기존 제출이 있을 때") {
            When("다시 제출하면") {
                Then("기존 row를 재사용해 PENDING으로 되돌린다") {
                    val cat = category(CategoryType.AWARD)
                    val existing =
                        freshScore(cat).copy(
                            scoreId = 77L,
                            scoreStatus = ScoreStatus.REJECTED,
                            rejectionReason = "증빙 부족",
                            activityName = "이전 내용",
                        )
                    every { memberUtil.getCurrentUserId() } returns userId
                    every { appendScoreSupport.resolveCategory(CategoryType.AWARD, EvidenceType.EVIDENCE) } returns cat
                    every { appendScoreSupport.findOrCreateScore(userId, cat) } returns existing
                    every { scorePersistencePort.save(any()) } answers { firstArg() }

                    val result = service.execute(CategoryType.AWARD, "수정된 내용")

                    result.scoreId shouldBe 77L
                    result.activityName shouldBe "수정된 내용"
                    result.rejectionReason shouldBe null
                    result.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }
        }
    })
