package team.incube.gsmc.domain.score.service

import io.kotest.assertions.throwables.shouldThrow
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
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.time.LocalDateTime

class FetchScoresByCategoryServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberPersistencePort = mockk<MemberPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchScoresByCategoryService(scorePersistencePort, memberPersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun categoryOf() =
            Category(
                categoryId = 1,
                weight = 1,
                categoryEnglishName = CategoryType.TOEIC.name,
                categoryKoreanName = "테스트카테고리",
                categoryMaximumValue = 10,
                isAccumulated = true,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = CategoryType.TOEIC,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        fun scoreOf(status: ScoreStatus) =
            Score(
                scoreId = 0,
                userId = 10L,
                category = categoryOf(),
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

        fun userOf(userGrade: Int?, userRole: UserRole = UserRole.STUDENT) =
            User (
                userId = 1L,
                userName = "강민우",
                userEmail = "absc123@gmail.com",
                userGrade = userGrade,
                userClassNumber = null,
                userNumber = null,
                userRole = userRole,
        )

        Given("특정 사용자의 카테고리 별 점수를 조회할 때"){

            When("권한이 교사 이상이고 사용자가 존재하면"){
                Then("카테고리 별 점수가 조회된다"){
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { memberPersistencePort.findByUserId(10L) } returns userOf(userGrade = 2)
                    every { scorePersistencePort.findAllByUserId(10L) } returns listOf(scoreOf(ScoreStatus.APPROVED))

                    val result = service.execute(10L, null)

                    result shouldHaveSize 1
                }
            }

            When("권한이 교사 이상이고 대상이 존재하지 않으면"){
                Then("USER_NOT_FOUND 예외가 발생한다"){
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { memberPersistencePort.findByUserId(10L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(10L, null) }
                    exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND

                }
            }

            When("권한이 학생이면"){
                Then("FORBIDDEN 예외가 발생한다"){
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT

                    val exception = shouldThrow<GsmcException> { service.execute(10L, null) }
                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                }
            }
        }
    })
