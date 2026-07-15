package team.incube.gsmc.domain.score

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import java.time.LocalDateTime

class ScoreCalculatorTest :
    BehaviorSpec({
        val userId = 1L

        val toeicCategory =
            Category(
                categoryId = 1,
                weight = 1,
                categoryEnglishName = "Toeic",
                categoryKoreanName = "TOEIC",
                categoryMaximumValue = 10,
                isAccumulated = false,
                evidenceType = EvidenceType.FILE,
                categoryType = CategoryType.TOEIC,
                calculationType = ScoreCalculationType.SCORE_BASED,
                conversionDivisor = 100,
            )

        val academyCategory =
            Category(
                categoryId = 2,
                weight = 1,
                categoryEnglishName = "Toeic Academy",
                categoryKoreanName = "토익사관학교",
                categoryMaximumValue = 1,
                isAccumulated = false,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = CategoryType.TOEIC_ACADEMY,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        fun score(
            category: Category,
            status: ScoreStatus,
            scoreValue: Int? = null,
        ) = Score(
            scoreId = 0L,
            userId = userId,
            category = category,
            evidence = null,
            file = null,
            scoreStatus = status,
            activityName = null,
            scoreValue = scoreValue,
            rejectionReason = null,
            dgProjectId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        Given("토익사관학교 가산점") {
            When("TOEIC 8점에 사관학교 참여가 승인되면") {
                Then("TOEIC 인정점수에 1점이 가산된다") {
                    val scores =
                        listOf(
                            score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 8),
                            score(academyCategory, ScoreStatus.APPROVED),
                        )

                    val groups = ScoreCalculator.categoryGroups(scores, statusFilter = null)
                    val toeicGroup = groups.first { it.categoryType == CategoryType.TOEIC }

                    toeicGroup.recognizedScore shouldBe 9
                }
            }

            When("TOEIC 9점에 사관학교 참여가 승인되어 캡(10점)을 초과하면") {
                Then("10점에서 멈춘다") {
                    val scores =
                        listOf(
                            score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 9),
                            score(academyCategory, ScoreStatus.APPROVED),
                        )

                    val total = ScoreCalculator.totalScoreOf(scores, includeApprovedOnly = true)

                    total shouldBe 10
                }
            }

            When("사관학교 참여가 미승인(PENDING) 상태면") {
                Then("가산되지 않는다") {
                    val scores =
                        listOf(
                            score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 8),
                            score(academyCategory, ScoreStatus.PENDING),
                        )

                    val groups = ScoreCalculator.categoryGroups(scores, statusFilter = null)
                    val toeicGroup = groups.first { it.categoryType == CategoryType.TOEIC }

                    toeicGroup.recognizedScore shouldBe 8
                }
            }

            When("사관학교 참여 제출이 아예 없으면") {
                Then("TOEIC 인정점수는 변환값 그대로다") {
                    val scores = listOf(score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 8))

                    val total = ScoreCalculator.totalScoreOf(scores, includeApprovedOnly = true)

                    total shouldBe 8
                }
            }

            Then("categoryGroups()가 반환하는 목록에 TOEIC_ACADEMY 그룹은 포함되지 않는다") {
                val scores =
                    listOf(
                        score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 8),
                        score(academyCategory, ScoreStatus.APPROVED),
                    )

                val groups = ScoreCalculator.categoryGroups(scores, statusFilter = null)

                groups shouldHaveSize 1
                groups.none { it.categoryType == CategoryType.TOEIC_ACADEMY } shouldBe true
            }

            Then("totalScoreOf()가 TOEIC_ACADEMY를 중복으로 합산하지 않는다") {
                val scores =
                    listOf(
                        score(toeicCategory, ScoreStatus.APPROVED, scoreValue = 8),
                        score(academyCategory, ScoreStatus.APPROVED),
                    )

                val total = ScoreCalculator.totalScoreOf(scores, includeApprovedOnly = true)

                total shouldBe 9
            }
        }
    })
