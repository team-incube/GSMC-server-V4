package team.incube.gsmc.domain.score.calculator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import java.time.LocalDateTime

class ToeicScoreCalculatorTest :
    BehaviorSpec({
        val userId = 1L
        val calculator = ToeicScoreCalculator()

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
            scoreValue: Int? = null,
        ) = Score(
            scoreId = 0L,
            userId = userId,
            category = category,
            evidence = null,
            file = null,
            scoreStatus = ScoreStatus.APPROVED,
            activityName = null,
            scoreValue = scoreValue,
            rejectionReason = null,
            dgProjectId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        Given("TOEIC 8점에 사관학교 참여 점수가 함께 주어지면") {
            When("recognizedScore를 계산하면") {
                Then("1점이 가산된다") {
                    val scoresByCategory =
                        mapOf(
                            toeicCategory to listOf(score(toeicCategory, scoreValue = 8)),
                            academyCategory to listOf(score(academyCategory)),
                        )

                    calculator.recognizedScore(scoresByCategory, toeicCategory) shouldBe 9
                }
            }
        }

        Given("TOEIC 9점에 사관학교 참여 점수가 더해져 캡(10점)을 초과하면") {
            When("recognizedScore를 계산하면") {
                Then("10점에서 멈춘다") {
                    val scoresByCategory =
                        mapOf(
                            toeicCategory to listOf(score(toeicCategory, scoreValue = 9)),
                            academyCategory to listOf(score(academyCategory)),
                        )

                    calculator.recognizedScore(scoresByCategory, toeicCategory) shouldBe 10
                }
            }
        }

        Given("사관학교 카테고리에 점수가 아예 없으면") {
            When("recognizedScore를 계산하면") {
                Then("보너스 없이 원점수 그대로다") {
                    val scoresByCategory = mapOf(toeicCategory to listOf(score(toeicCategory, scoreValue = 8)))

                    calculator.recognizedScore(scoresByCategory, toeicCategory) shouldBe 8
                }
            }
        }

        Given("사관학교 카테고리는 있지만 점수 리스트가 비어 있으면") {
            When("recognizedScore를 계산하면") {
                Then("보너스가 가산되지 않는다") {
                    val scoresByCategory =
                        mapOf(
                            toeicCategory to listOf(score(toeicCategory, scoreValue = 8)),
                            academyCategory to emptyList(),
                        )

                    calculator.recognizedScore(scoresByCategory, toeicCategory) shouldBe 8
                }
            }
        }
    })
