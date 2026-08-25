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
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class FetchScoresByDgProjectIdServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchScoresByDgProjectIdService(scorePersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        val dgProjectId = 100L
        val category =
            Category(
                categoryId = 1,
                weight = 2,
                categoryEnglishName = "PROJECT_PARTICIPATION",
                categoryKoreanName = "프로젝트 참여",
                categoryMaximumValue = 10,
                isAccumulated = true,
                evidenceType = EvidenceType.EVIDENCE,
                categoryType = CategoryType.PROJECT_PARTICIPATION,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        fun score(userId: Long) =
            Score(
                scoreId = userId,
                userId = userId,
                category = category,
                evidence = null,
                file = null,
                scoreStatus = ScoreStatus.PENDING,
                activityName = "DataGSM 프로젝트",
                scoreValue = null,
                rejectionReason = null,
                dgProjectId = dgProjectId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        Given("교사 이상 권한으로") {
            When("같은 프로젝트로 제출한 사람들을 조회하면") {
                Then("전원의 점수 목록을 반환한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findAllByDgProjectId(dgProjectId) } returns
                        listOf(score(1L), score(2L))

                    val result = service.execute(dgProjectId)

                    result.size shouldBe 2
                }
            }
        }

        Given("학생 권한으로") {
            When("조회를 시도하면") {
                Then("FORBIDDEN 예외가 발생한다") {
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT

                    val exception = shouldThrow<GsmcException> { service.execute(dgProjectId) }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                }
            }
        }
    })
