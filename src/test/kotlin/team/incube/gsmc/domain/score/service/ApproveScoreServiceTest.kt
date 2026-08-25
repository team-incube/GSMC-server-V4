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
import team.incube.gsmc.domain.alert.Alert
import team.incube.gsmc.domain.alert.AlertType
import team.incube.gsmc.domain.alert.port.out.AlertEventPublisherPort
import team.incube.gsmc.domain.alert.port.out.AlertPersistencePort
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

class ApproveScoreServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val alertPersistencePort = mockk<AlertPersistencePort>()
        val alertEventPublisherPort = mockk<AlertEventPublisherPort>()
        val scoreTotalCacheInvalidator = mockk<ScoreTotalCacheInvalidator>()
        val memberUtil = mockk<MemberUtil>()
        val service =
            ApproveScoreService(
                scorePersistencePort,
                alertPersistencePort,
                alertEventPublisherPort,
                scoreTotalCacheInvalidator,
                memberUtil,
            )

        beforeEach {
            clearAllMocks()
            every { scoreTotalCacheInvalidator.invalidate(any()) } just runs
        }

        fun score(status: ScoreStatus) =
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
                scoreStatus = status,
                activityName = "수상 내역",
                scoreValue = null,
                rejectionReason = null,
                dgProjectId = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        Given("교사 이상 권한으로") {
            When("PENDING 상태의 점수를 승인하면") {
                Then("상태를 APPROVED로 갈아끼워 저장하고 APPROVED 알림을 생성한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(1L) } returns score(ScoreStatus.PENDING)
                    every { scorePersistencePort.save(any()) } answers { firstArg() }
                    every { alertPersistencePort.save(any()) } answers { firstArg<Alert>().copy(alertId = 100L) }
                    every { alertEventPublisherPort.publish(any()) } just runs

                    val result = service.execute(1L)

                    result shouldBe true
                    verify(exactly = 1) {
                        scorePersistencePort.save(
                            match {
                                it.scoreStatus == ScoreStatus.APPROVED &&
                                    it.rejectionReason == null
                            },
                        )
                    }
                    verify(exactly = 1) {
                        alertPersistencePort.save(
                            match<Alert> {
                                it.userId == 10L &&
                                    it.scoreId == 1L &&
                                    it.alertType == AlertType.APPROVED &&
                                    it.content.contains("수상경력")
                            },
                        )
                    }
                    verify(exactly = 1) {
                        alertEventPublisherPort.publish(match<Alert> { it.alertId == 100L })
                    }
                }
            }

            When("이미 APPROVED인 점수를 다시 승인하면") {
                Then("상태는 다시 저장하지만 알림은 중복 생성하지 않는다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(1L) } returns score(ScoreStatus.APPROVED)
                    every { scorePersistencePort.save(any()) } answers { firstArg() }

                    val result = service.execute(1L)

                    result shouldBe true
                    verify(exactly = 1) { scorePersistencePort.save(any()) }
                    verify(exactly = 0) { alertPersistencePort.save(any()) }
                    verify(exactly = 0) { alertEventPublisherPort.publish(any()) }
                    verify(exactly = 0) { scoreTotalCacheInvalidator.invalidate(any()) }
                }
            }

            When("존재하지 않는 점수를 승인하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L) }

                    exception.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                    verify(exactly = 0) { alertPersistencePort.save(any()) }
                    verify(exactly = 0) { alertEventPublisherPort.publish(any()) }
                }
            }
        }

        Given("학생 권한으로") {
            When("승인을 시도하면") {
                Then("FORBIDDEN 예외가 발생하고 조회조차 하지 않는다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT

                    val exception = shouldThrow<GsmcException> { service.execute(1L) }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                    verify(exactly = 0) { scorePersistencePort.findById(any()) }
                }
            }
        }
    })
