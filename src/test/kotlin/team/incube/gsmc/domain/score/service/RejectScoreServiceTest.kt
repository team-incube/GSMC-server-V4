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
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class RejectScoreServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = RejectScoreService(scorePersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun pendingScore() =
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
                scoreStatus = ScoreStatus.PENDING,
                activityName = "수상 내역",
                scoreValue = null,
                rejectionReason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        Given("교사 이상 권한으로") {
            When("존재하는 점수를 거절하면") {
                Then("상태를 REJECTED로, 거절 사유를 함께 저장한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(1L) } returns pendingScore()
                    every { scorePersistencePort.save(any()) } answers { firstArg() }

                    val result = service.execute(1L, "증빙자료가 부족합니다")

                    result shouldBe true
                    verify(exactly = 1) {
                        scorePersistencePort.save(
                            match { it.scoreStatus == ScoreStatus.REJECTED && it.rejectionReason == "증빙자료가 부족합니다" },
                        )
                    }
                }
            }

            When("존재하지 않는 점수를 거절하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L, "사유") }

                    exception.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }

        Given("학생 권한으로") {
            When("거절을 시도하면") {
                Then("FORBIDDEN 예외가 발생한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT

                    val exception = shouldThrow<GsmcException> { service.execute(1L, "사유") }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                }
            }
        }
    })
