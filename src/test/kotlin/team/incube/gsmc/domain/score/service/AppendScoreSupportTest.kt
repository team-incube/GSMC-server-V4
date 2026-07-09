package team.incube.gsmc.domain.score.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.time.LocalDateTime

class AppendScoreSupportTest :
    BehaviorSpec({
        val categoryPersistencePort = mockk<CategoryPersistencePort>()
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val support = AppendScoreSupport(categoryPersistencePort, scorePersistencePort)

        beforeEach { clearAllMocks() }

        val userId = 1L

        fun category(
            evidenceType: EvidenceType,
            calculationType: ScoreCalculationType,
        ) = Category(
            categoryId = 1,
            weight = 1,
            categoryEnglishName = "CERTIFICATE",
            categoryKoreanName = "자격증",
            categoryMaximumValue = 14,
            isAccumulated = true,
            evidenceType = evidenceType,
            categoryType = CategoryType.CERTIFICATE,
            calculationType = calculationType,
        )

        Given("resolveCategory") {
            When("카테고리가 없으면") {
                Then("CATEGORY_NOT_FOUND 예외가 발생한다") {
                    every { categoryPersistencePort.findByCategoryType(CategoryType.CERTIFICATE) } returns null

                    val exception =
                        shouldThrow<GsmcException> {
                            support.resolveCategory(CategoryType.CERTIFICATE, EvidenceType.FILE)
                        }

                    exception.errorCode shouldBe ErrorCode.CATEGORY_NOT_FOUND
                }
            }

            When("증빙 방식이 다르면") {
                Then("INVALID_CATEGORY_TYPE 예외가 발생한다") {
                    every { categoryPersistencePort.findByCategoryType(CategoryType.CERTIFICATE) } returns
                        category(EvidenceType.EVIDENCE, ScoreCalculationType.COUNT_BASED)

                    val exception =
                        shouldThrow<GsmcException> {
                            support.resolveCategory(CategoryType.CERTIFICATE, EvidenceType.FILE)
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_CATEGORY_TYPE
                }
            }

            When("증빙 방식이 일치하면") {
                Then("카테고리를 그대로 반환한다") {
                    val cat = category(EvidenceType.FILE, ScoreCalculationType.COUNT_BASED)
                    every { categoryPersistencePort.findByCategoryType(CategoryType.CERTIFICATE) } returns cat

                    support.resolveCategory(CategoryType.CERTIFICATE, EvidenceType.FILE) shouldBe cat
                }
            }
        }

        Given("resolveUnrequiredCategory") {
            When("증빙 방식이 UNREQUIRED가 아니면") {
                Then("INVALID_CATEGORY_TYPE 예외가 발생한다") {
                    every { categoryPersistencePort.findByCategoryType(CategoryType.VOLUNTEER) } returns
                        category(EvidenceType.FILE, ScoreCalculationType.SCORE_BASED)

                    val exception =
                        shouldThrow<GsmcException> {
                            support.resolveUnrequiredCategory(CategoryType.VOLUNTEER, ScoreCalculationType.SCORE_BASED)
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_CATEGORY_TYPE
                }
            }

            When("집계 방식이 다르면") {
                Then("INVALID_CATEGORY_TYPE 예외가 발생한다") {
                    every { categoryPersistencePort.findByCategoryType(CategoryType.VOLUNTEER) } returns
                        category(EvidenceType.UNREQUIRED, ScoreCalculationType.COUNT_BASED)

                    val exception =
                        shouldThrow<GsmcException> {
                            support.resolveUnrequiredCategory(CategoryType.VOLUNTEER, ScoreCalculationType.SCORE_BASED)
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_CATEGORY_TYPE
                }
            }

            When("증빙 방식과 집계 방식이 모두 일치하면") {
                Then("카테고리를 그대로 반환한다") {
                    val cat = category(EvidenceType.UNREQUIRED, ScoreCalculationType.SCORE_BASED)
                    every { categoryPersistencePort.findByCategoryType(CategoryType.VOLUNTEER) } returns cat

                    support.resolveUnrequiredCategory(CategoryType.VOLUNTEER, ScoreCalculationType.SCORE_BASED) shouldBe
                        cat
                }
            }
        }

        Given("parseScoreValue") {
            When("숫자 문자열이 주어지면") {
                Then("정수로 파싱한다") {
                    support.parseScoreValue("850") shouldBe 850
                }
            }

            When("null이 주어지면") {
                Then("INVALID_SCORE_VALUE 예외가 발생한다") {
                    val exception = shouldThrow<GsmcException> { support.parseScoreValue(null) }
                    exception.errorCode shouldBe ErrorCode.INVALID_SCORE_VALUE
                }
            }

            When("숫자가 아닌 문자열이 주어지면") {
                Then("INVALID_SCORE_VALUE 예외가 발생한다") {
                    val exception = shouldThrow<GsmcException> { support.parseScoreValue("전국 1위") }
                    exception.errorCode shouldBe ErrorCode.INVALID_SCORE_VALUE
                }
            }
        }

        Given("findOrCreateScore") {
            When("재사용 가능한 REJECTED 점수가 있으면") {
                Then("그 점수를 그대로 반환한다") {
                    val cat = category(EvidenceType.FILE, ScoreCalculationType.SCORE_BASED)
                    val rejected =
                        Score(
                            scoreId = 5L,
                            userId = userId,
                            category = cat,
                            evidence = null,
                            file = null,
                            scoreStatus = ScoreStatus.REJECTED,
                            activityName = null,
                            scoreValue = 100,
                            rejectionReason = "사유",
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now(),
                        )
                    every {
                        scorePersistencePort.findByUserIdAndCategoryTypeAndScoreStatus(
                            userId,
                            cat.categoryType,
                            ScoreStatus.REJECTED,
                        )
                    } returns rejected

                    support.findOrCreateScore(userId, cat) shouldBe rejected
                }
            }

            When("재사용 가능한 REJECTED 점수가 없으면") {
                Then("scoreId가 0인 새 점수를 반환한다") {
                    val cat = category(EvidenceType.FILE, ScoreCalculationType.SCORE_BASED)
                    every {
                        scorePersistencePort.findByUserIdAndCategoryTypeAndScoreStatus(
                            userId,
                            cat.categoryType,
                            ScoreStatus.REJECTED,
                        )
                    } returns null

                    val result = support.findOrCreateScore(userId, cat)

                    result.scoreId shouldBe 0L
                    result.userId shouldBe userId
                    result.scoreStatus shouldBe ScoreStatus.PENDING
                    result.evidence shouldBe null
                    result.scoreValue shouldBe null
                    result.activityName shouldBe null
                }
            }
        }
    })
