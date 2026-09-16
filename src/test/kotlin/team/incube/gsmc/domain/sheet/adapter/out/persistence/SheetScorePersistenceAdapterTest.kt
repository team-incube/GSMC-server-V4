package team.incube.gsmc.domain.sheet.adapter.out.persistence

import com.querydsl.core.types.Predicate
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

class SheetScorePersistenceAdapterTest :
    BehaviorSpec({
        val queryFactory = mockk<JPAQueryFactory>()
        val adapter = SheetScorePersistenceAdapter(queryFactory)

        beforeEach { clearAllMocks() }

        val categoryId = 2L

        fun userEntity(userId: Long) =
            UserJpaEntity(
                userId = userId,
                userName = "학생$userId",
                userEmail = "student$userId@gsm.hs.kr",
                userGrade = 2,
                userClassNumber = 3,
                userNumber = userId.toInt(),
                userRole = UserRole.STUDENT,
            )

        fun categoryEntity() =
            CategoryJpaEntity(
                categoryId = categoryId,
                weight = 1,
                categoryEnglishName = "TOEIC",
                categoryKoreanName = "토익",
                categoryMaximumValue = 10,
                isAccumulated = false,
                evidenceType = EvidenceType.FILE,
                categoryType = CategoryType.TOEIC,
                calculationType = ScoreCalculationType.SCORE_BASED,
                conversionDivisor = 100,
            )

        fun scoreEntity(
            scoreId: Long,
            userId: Long,
        ) = ScoreJpaEntity(
            scoreId = scoreId,
            user = userEntity(userId),
            category = categoryEntity(),
            evidence = null,
            scoreStatus = ScoreStatus.APPROVED,
            activityName = null,
            scoreValue = 900,
            rejectionReason = null,
            dgProjectId = null,
        )

        Given("findApprovedScoresByUserIds로 승인된 점수를 조회할 때") {
            When("전달된 사용자 ID 목록이 비어 있으면") {
                Then("쿼리를 실행하지 않고 빈 맵을 반환한다") {
                    adapter.findApprovedScoresByUserIds(emptyList()).shouldBeEmpty()
                }
            }

            When("여러 사용자의 승인된 점수가 존재하면") {
                Then("사용자 ID별로 그룹화한 맵을 반환한다") {
                    val query = mockk<JPAQuery<ScoreJpaEntity>>()
                    every { queryFactory.selectFrom(scoreJpaEntity) } returns query
                    every { query.join(scoreJpaEntity.user) } returns query
                    every { query.join(scoreJpaEntity.category) } returns query
                    every { query.leftJoin(scoreJpaEntity.evidence) } returns query
                    every { query.fetchJoin() } returns query
                    every { query.where(*anyVararg<Predicate>()) } returns query
                    every { query.fetch() } returns
                        listOf(
                            scoreEntity(1L, userId = 10L),
                            scoreEntity(2L, userId = 10L),
                            scoreEntity(3L, userId = 20L),
                        )

                    val result = adapter.findApprovedScoresByUserIds(listOf(10L, 20L))

                    result.keys shouldBe setOf(10L, 20L)
                    result[10L]?.map { it.scoreId } shouldBe listOf(1L, 2L)
                    result[20L]?.map { it.scoreId } shouldBe listOf(3L)
                }
            }
        }
    })
