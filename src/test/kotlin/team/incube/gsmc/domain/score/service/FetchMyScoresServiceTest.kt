package team.incube.gsmc.domain.score.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
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

class FetchMyScoresServiceTest :
    BehaviorSpec ({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchMyScoresService(scorePersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun categoryOf(categoryType: CategoryType) =
            Category(
                categoryId = 1,
                weight = 1,
                categoryEnglishName = categoryType.name,
                categoryKoreanName = "테스트카테고리",
                categoryMaximumValue = 10,
                isAccumulated = true,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = categoryType,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        fun scoreOf(categoryType: CategoryType, status: ScoreStatus) =
            Score(
                scoreId = 0,
                userId = 10L,
                category = categoryOf(categoryType),
                evidence = null,
                file = null,
                scoreStatus = status,
                activityName = null,
                scoreValue = null,
                rejectionReason = null,
                dgProjectId = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        Given("사용자가 서로 다른 카테고리 / 상태의 점수를 여러개 가지고 있을 때"){
             val scores =
                listOf(
                    scoreOf(CategoryType.TOEIC, ScoreStatus.PENDING),
                    scoreOf(CategoryType.TOEIC, ScoreStatus.APPROVED),
                    scoreOf(CategoryType.JLPT, ScoreStatus.PENDING),
                )
            When("필터 없이 조회하면"){
                Then("전체 점수가 반환된다"){

                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { scorePersistencePort.findAllByUserId(10L) } returns scores

                    val result = service.execute(null, null)
                    result shouldHaveSize 3
                }
            }

            When("카테고리로 필터링해서 조회하면"){
                Then("해당 카테고리 점수만 반환된다"){
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { scorePersistencePort.findAllByUserId(10L) } returns scores

                    val result = service.execute(CategoryType.TOEIC, null)
                    result shouldHaveSize 2
                    result.all { it.category.categoryType == CategoryType.TOEIC } shouldBe true
                }
            }

            When("상태로 필터링해서 조회하면"){
                Then("해당 상태 점수만 반환된다"){
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { scorePersistencePort.findAllByUserId(10L) } returns scores

                    val result = service.execute(null, ScoreStatus.PENDING)
                    result shouldHaveSize 2
                    result.all { it.scoreStatus == ScoreStatus.PENDING } shouldBe true
                }
            }

            When("카테고리와 상태 둘 다로 필터링해서 조회하면"){
                Then("둘 다 만족하는 점수만 반환된다"){
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { scorePersistencePort.findAllByUserId(10L) } returns scores

                    val result = service.execute(CategoryType.TOEIC, ScoreStatus.PENDING)
                    result shouldHaveSize 1
                    result.all { it.category.categoryType == CategoryType.TOEIC && it.scoreStatus == ScoreStatus.PENDING } shouldBe true
                }
            }
        }

        Given("사용자가 점수를 하나도 가지고 있지 않을 때"){
            When("조회하면"){
                Then("빈 리스트가 반환된다"){
                    every { memberUtil.getCurrentUserId() } returns 9L
                    every { scorePersistencePort.findAllByUserId(9L) } returns emptyList()

                    val result = service.execute(null, null)
                    result shouldHaveSize 0
                }
            }
        }
    })
