package team.incube.gsmc.domain.score.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

class FetchMyScoresByCategoryServiceTest :
    BehaviorSpec({
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberPersistencePort = mockk<MemberPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchMyScoresByCategoryService(scorePersistencePort, memberPersistencePort, memberUtil)

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

        fun scoreOf(status: ScoreStatus, categoryType: CategoryType) =
            Score(
                scoreId = 0,
                userId = 1L,
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

        fun userOf(userGrade: Int? = 1) =
            User(
                userId = 1L,
                userName = "강민우",
                userEmail = "absc123@gmail.com",
                userGrade = userGrade,
                userClassNumber = null,
                userNumber = null,
                userRole = UserRole.STUDENT,
            )

        Given("본인이 여러 카테고리의 점수를 가지고 있을 때"){
            When("status 없이 조회하면"){
                Then("카테고리 개수만큼 그룹이 반환된다"){
                    val scores =
                        listOf(
                            scoreOf(ScoreStatus.APPROVED, CategoryType.TOEIC),
                            scoreOf(ScoreStatus.APPROVED, CategoryType.JLPT),
                            scoreOf(ScoreStatus.PENDING, CategoryType.ACADEMIC_GRADE),
                        )
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns userOf()
                    every { scorePersistencePort.findAllByUserId(1L) } returns scores

                    val result = service.execute(null)

                    result shouldHaveSize 3
                    result.map { it.categoryType } shouldContainExactlyInAnyOrder
                        listOf(CategoryType.TOEIC, CategoryType.JLPT, CategoryType.ACADEMIC_GRADE) //실제 가지고 있는 카테고리들인지 확인
                }
            }
            When("status = ScoreStatus.PENDING 등으로 조회하면"){
                Then("반환된 그룹의 scores 필드가 필터링되어 있다"){
                    val scores =
                        listOf(
                            scoreOf(ScoreStatus.APPROVED, CategoryType.TOEIC),
                            scoreOf(ScoreStatus.APPROVED, CategoryType.JLPT),
                            scoreOf(ScoreStatus.PENDING, CategoryType.ACADEMIC_GRADE),
                        )

                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns userOf()
                    every { scorePersistencePort.findAllByUserId(1L) } returns scores

                    val result = service.execute(ScoreStatus.PENDING)

                    result shouldHaveSize 3
                    result.flatMap { it.scores }.all { it.scoreStatus == ScoreStatus.PENDING } shouldBe true

                }
            }
        }

        Given("내 카테고리별 점수를 조회할 때"){
            When("회원이 존재하지 않으면"){
                Then("GsmcException(ErrorCode.USER_NOT_FOUND) 발생"){
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(null) }
                    exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
                }
            }

            When("점수가 하나도 없으면"){
                Then("빈 리스트를 반환한다"){
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns userOf()
                    every { scorePersistencePort.findAllByUserId(1L) } returns listOf()

                    val result = service.execute(null)
                    result shouldBe emptyList()
                }
            }
        }

        Given("뉴로우참여 점수를 가진 유저를 조회할 때"){
            val scores =
                listOf(
                    scoreOf(ScoreStatus.APPROVED, CategoryType.TOEIC),
                    scoreOf(ScoreStatus.APPROVED, CategoryType.JLPT),
                    scoreOf(ScoreStatus.APPROVED, CategoryType.NEWRROW_SCHOOL),
                )
            When("1학년이면"){
                Then("뉴로우 참여 카테고리가 결과에 포함된다"){
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns userOf(1)
                    every { scorePersistencePort.findAllByUserId(1L) } returns scores

                    val result = service.execute(null)

                    result.map { it.categoryType } shouldContainExactlyInAnyOrder
                        listOf(CategoryType.TOEIC, CategoryType.JLPT, CategoryType.NEWRROW_SCHOOL)
                }
            }

            When("2학년 이상이면"){
                Then("뉴로우 참여 카테고리가 결과에서 제외된다"){
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { memberPersistencePort.findByUserId(1L) } returns userOf(2)
                    every { scorePersistencePort.findAllByUserId(1L) } returns scores

                    val result = service.execute(null)

                    result.map { it.categoryType } shouldContainExactlyInAnyOrder
                        listOf(CategoryType.TOEIC, CategoryType.JLPT)
                }
            }
        }
    })
