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

class FetchScoreServiceTest :
    BehaviorSpec({
        // 1. mock 대상 준비 (Service가 생성자로 받는 의존성 2개)
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = FetchScoreService(scorePersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        // 2. 테스트용 Score 객체를 만드는 헬퍼 함수
        //    ApproveScoreServiceTest의 pendingScore()를 참고해서
        //    userId를 파라미터로 받도록 만들어보세요 (소유자 검증 케이스에 필요)
        val academicGradeCategory =
            Category(
                categoryId = 2,
                weight = 1,
                categoryEnglishName = "ACADEMIC_GRADE",
                categoryKoreanName = "교과성적",
                categoryMaximumValue = 9,
                isAccumulated = false,
                evidenceType = EvidenceType.UNREQUIRED,
                categoryType = CategoryType.ACADEMIC_GRADE,
                calculationType = ScoreCalculationType.SCORE_BASED,
            )

        fun scoreOwnedBy(userId: Long) =
            Score(
                scoreId = 0,
                userId = userId,
                category = academicGradeCategory,
                evidence = null,
                file = null,
                scoreStatus = ScoreStatus.PENDING,
                activityName = null,
                scoreValue = null,
                rejectionReason = null,
                dgProjectId = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        // 3. 케이스 1 — 본인 소유 점수를 조회하면 정상 반환된다
        Given("본인 소유의 점수 요청에 대해") {
            When("본인이 조회하면") {
                Then("점수가 정상 반환된다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT
                    val ownScore = scoreOwnedBy(10L)
                    every { scorePersistencePort.findById(1L) } returns ownScore
                    val result = service.execute(1L)

                    result shouldBe ownScore
                }
            }
        }

        // 4. 케이스 2 — 교사 이상 권한이면 남의 점수도 조회 가능하다
        Given("타인 소유의 점수 요청에 대해") {
            When("교사 이상 권한으로 조회하면") {
                Then("점수가 정상 반환된다") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    val ownScore = scoreOwnedBy(9L)
                    every { memberUtil.getCurrentUserRole() } returns UserRole.TEACHER
                    every { scorePersistencePort.findById(1L) } returns ownScore

                    // TODO: 결과 검증
                    val result = service.execute(1L)

                    result shouldBe ownScore
                }
            }

            // 5. 케이스 3 — 학생이 남의 점수를 조회하면 FORBIDDEN
            When("학생 권한으로 조회하면") {
                Then("FORBIDDEN 예외가 발생한다") {
                    // TODO: shouldThrow<GsmcException> { service.execute(...) }
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { memberUtil.getCurrentUserRole() } returns UserRole.STUDENT
                    every { scorePersistencePort.findById(1L) } returns scoreOwnedBy(9L)
                    // TODO: exception.errorCode shouldBe ErrorCode.FORBIDDEN
                    val exception = shouldThrow<GsmcException> { service.execute(1L) }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                }
            }
        }

        // 6. 케이스 4 — 존재하지 않는 scoreId
        Given("존재하지 않는 점수 요청 ID에 대해") {
            When("조회하면") {
                Then("SCORE_NOT_FOUND 예외가 발생한다") {
                    every { scorePersistencePort.findById(1L) } returns null
                    val exception = shouldThrow<GsmcException> { service.execute(1L) }
                    exception.errorCode shouldBe ErrorCode.SCORE_NOT_FOUND
                }
            }
        }
    })
