package team.incube.gsmc.domain.score.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class RemoveScoreServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = RemoveScoreService(scorePersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun approvedScore() =
            Score(
                scoreId = 1L,
                userId = 10L,
                category =
                    Category(
                        categoryId = 1,
                        weight = 1,
                        categoryEnglishName = "AWARD",
                        categoryKoreanName = "수상경력",
                        categoryMaximumValue = 10,
                        isAccumulated = true,
                        evidenceType = EvidenceType.EVIDENCE,
                        categoryType = CategoryType.AWARD,
                        calculationType = ScoreCalculationType.COUNT_BASED,
                    ),
                evidence = null,
                file = null,
                scoreStatus = ScoreStatus.APPROVED,
                activityName = "수상 내역",
                scoreValue = null,
                rejectionReason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        Given("교사 이상 권한으로") {
            When("이미 승인된 점수를 삭제해도") {
                Then("상태와 무관하게 삭제된다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(1L) } returns approvedScore()
                    every { scorePersistencePort.deleteById(1L) } just runs

                    val result = service.execute(1L)

                    result shouldBe true
                    verify(exactly = 1) { scorePersistencePort.deleteById(1L) }
                }
            }

            When("존재하지 않는 점수를 삭제하면") {
                Then("SCORE_NOT_FOUND 예외가 발생하고 삭제는 호출되지 않는다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L) }

                    exception.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                    verify(exactly = 0) { scorePersistencePort.deleteById(any()) }
                }
            }
        }

        Given("학생 권한으로") {
            When("삭제를 시도하면") {
                Then("FORBIDDEN 예외가 발생한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT

                    val exception = shouldThrow<GsmcException> { service.execute(1L) }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                }
            }
        }
    })
