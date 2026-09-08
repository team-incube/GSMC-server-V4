package team.incube.gsmc.domain.score.adapter.out.persistence

import com.querydsl.core.types.Predicate
import com.querydsl.jpa.impl.JPADeleteClause
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.querydsl.jpa.impl.JPAUpdateClause
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.springframework.dao.DataIntegrityViolationException
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.QFileJpaEntity.fileJpaEntity
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.ScoreStatus
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreJpaEntity.scoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.QScoreUniqueSlotJpaEntity.scoreUniqueSlotJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreSlotKind
import team.incube.gsmc.domain.score.adapter.out.persistence.repository.ScoreJpaRepository
import team.incube.gsmc.domain.score.adapter.out.persistence.repository.ScoreUniqueSlotJpaRepository
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.time.LocalDateTime

class ScorePersistenceAdapterTest :
    BehaviorSpec({
        val queryFactory = mockk<JPAQueryFactory>()
        val scoreJpaRepository = mockk<ScoreJpaRepository>()
        val scoreUniqueSlotJpaRepository = mockk<ScoreUniqueSlotJpaRepository>()
        val entityManager = mockk<EntityManager>()
        val adapter =
            ScorePersistenceAdapter(queryFactory, scoreJpaRepository, scoreUniqueSlotJpaRepository, entityManager)

        beforeEach { clearAllMocks() }

        val userId = 1L
        val categoryId = 2L

        fun userEntity() =
            UserJpaEntity(
                userId = userId,
                userName = "학생",
                userEmail = "student@gsm.hs.kr",
                userGrade = 2,
                userClassNumber = 3,
                userNumber = 4,
                userRole = UserRole.STUDENT,
            )

        fun categoryEntity(isAccumulated: Boolean) =
            CategoryJpaEntity(
                categoryId = categoryId,
                weight = 1,
                categoryEnglishName = "TOEIC",
                categoryKoreanName = "토익",
                categoryMaximumValue = 10,
                isAccumulated = isAccumulated,
                evidenceType = EvidenceType.FILE,
                categoryType = CategoryType.TOEIC,
                calculationType = ScoreCalculationType.SCORE_BASED,
                conversionDivisor = 100,
            )

        fun category(isAccumulated: Boolean) =
            Category(
                categoryId = categoryId,
                weight = 1,
                categoryEnglishName = "TOEIC",
                categoryKoreanName = "토익",
                categoryMaximumValue = 10,
                isAccumulated = isAccumulated,
                evidenceType = EvidenceType.FILE,
                categoryType = CategoryType.TOEIC,
                calculationType = ScoreCalculationType.SCORE_BASED,
                conversionDivisor = 100,
            )

        fun scoreEntity(
            scoreId: Long,
            status: ScoreStatus,
            isAccumulated: Boolean = false,
        ) = ScoreJpaEntity(
            scoreId = scoreId,
            user = userEntity(),
            category = categoryEntity(isAccumulated),
            evidence = null,
            scoreStatus = status,
            activityName = null,
            scoreValue = 900,
            rejectionReason = null,
            dgProjectId = null,
        )

        fun score(
            scoreId: Long,
            status: ScoreStatus,
            isAccumulated: Boolean = false,
        ) = Score(
            scoreId = scoreId,
            userId = userId,
            category = category(isAccumulated),
            evidence = null,
            file = null,
            scoreStatus = status,
            activityName = null,
            scoreValue = 900,
            rejectionReason = null,
            dgProjectId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        fun mockFindByUserIdAndCategoryType(result: ScoreJpaEntity?) {
            val query = mockk<JPAQuery<ScoreJpaEntity>>()
            every { queryFactory.selectFrom(scoreJpaEntity) } returns query
            every { query.join(scoreJpaEntity.user) } returns query
            every { query.join(scoreJpaEntity.category) } returns query
            every { query.leftJoin(scoreJpaEntity.evidence) } returns query
            every { query.fetchJoin() } returns query
            every { query.where(*anyVararg<Predicate>()) } returns query
            every { query.fetchFirst() } returns result

            val fileQuery = mockk<JPAQuery<team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity>>()
            every { queryFactory.selectFrom(fileJpaEntity) } returns fileQuery
            every { fileQuery.where(any<Predicate>()) } returns fileQuery
            every { fileQuery.fetchFirst() } returns null
        }

        Given("findUnapprovedByUserIdAndCategoryType로 조회할 때") {
            When("승인되지 않은 점수가 존재하면") {
                Then("도메인 객체로 변환해 반환한다") {
                    mockFindByUserIdAndCategoryType(scoreEntity(10L, ScoreStatus.PENDING))

                    val result = adapter.findUnapprovedByUserIdAndCategoryType(userId, CategoryType.TOEIC)

                    result?.scoreId shouldBe 10L
                    result?.scoreStatus shouldBe ScoreStatus.PENDING
                }
            }

            When("승인되지 않은 점수가 없으면") {
                Then("null을 반환한다") {
                    mockFindByUserIdAndCategoryType(null)

                    adapter.findUnapprovedByUserIdAndCategoryType(userId, CategoryType.TOEIC).shouldBeNull()
                }
            }
        }

        Given("findApprovedByUserIdAndCategoryType로 조회할 때") {
            When("승인된 점수가 존재하면") {
                Then("도메인 객체로 변환해 반환한다") {
                    mockFindByUserIdAndCategoryType(scoreEntity(11L, ScoreStatus.APPROVED))

                    val result = adapter.findApprovedByUserIdAndCategoryType(userId, CategoryType.TOEIC)

                    result?.scoreId shouldBe 11L
                    result?.scoreStatus shouldBe ScoreStatus.APPROVED
                }
            }

            When("승인된 점수가 없으면") {
                Then("null을 반환한다") {
                    mockFindByUserIdAndCategoryType(null)

                    adapter.findApprovedByUserIdAndCategoryType(userId, CategoryType.TOEIC).shouldBeNull()
                }
            }
        }

        Given("save로 저장할 때") {
            fun mockBaseSave(
                scoreToSave: Score,
                savedEntity: ScoreJpaEntity,
            ) {
                every {
                    entityManager.getReference(UserJpaEntity::class.java, scoreToSave.userId)
                } returns userEntity()
                every {
                    entityManager.getReference(CategoryJpaEntity::class.java, scoreToSave.category.categoryId)
                } returns categoryEntity(scoreToSave.category.isAccumulated)
                every { scoreJpaRepository.save(any()) } returns savedEntity
            }

            When("비누적 카테고리의 신규 점수를 저장하면") {
                Then("새 슬롯을 saveAndFlush로 점유한다") {
                    val newScore = score(0L, ScoreStatus.PENDING, isAccumulated = false)
                    val saved = scoreEntity(100L, ScoreStatus.PENDING, isAccumulated = false)
                    mockBaseSave(newScore, saved)
                    val savedSlot =
                        slot<team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreUniqueSlotJpaEntity>()
                    every { scoreUniqueSlotJpaRepository.saveAndFlush(capture(savedSlot)) } answers
                        { savedSlot.captured }

                    val result = adapter.save(newScore)

                    result.scoreId shouldBe 100L
                    savedSlot.captured.scoreId shouldBe 100L
                    savedSlot.captured.userId shouldBe userId
                    savedSlot.captured.categoryId shouldBe categoryId
                    savedSlot.captured.slotKind shouldBe ScoreSlotKind.UNAPPROVED
                    verify(exactly = 1) { scoreUniqueSlotJpaRepository.saveAndFlush(any()) }
                    verify(exactly = 0) { queryFactory.update(scoreUniqueSlotJpaEntity) }
                }
            }

            When("비누적 카테고리의 기존 점수 상태가 바뀌어 저장되면") {
                Then("QueryDSL 벌크 UPDATE로 슬롯 종류를 갱신한다") {
                    val existingScore = score(5L, ScoreStatus.APPROVED, isAccumulated = false)
                    val saved = scoreEntity(5L, ScoreStatus.APPROVED, isAccumulated = false)
                    mockBaseSave(existingScore, saved)
                    val updateClause = mockk<JPAUpdateClause>()
                    every { queryFactory.update(scoreUniqueSlotJpaEntity) } returns updateClause
                    every {
                        updateClause.set(scoreUniqueSlotJpaEntity.slotKind, ScoreSlotKind.APPROVED)
                    } returns updateClause
                    every { updateClause.where(*anyVararg<Predicate>()) } returns updateClause
                    every { updateClause.execute() } returns 1L

                    val result = adapter.save(existingScore)

                    result.scoreId shouldBe 5L
                    verify(exactly = 1) { updateClause.execute() }
                    verify(exactly = 0) { scoreUniqueSlotJpaRepository.saveAndFlush(any()) }
                }
            }

            When("누적 카테고리 점수를 저장하면") {
                Then("슬롯 동기화를 전혀 수행하지 않는다") {
                    val accumulatedScore = score(0L, ScoreStatus.PENDING, isAccumulated = true)
                    val saved = scoreEntity(200L, ScoreStatus.PENDING, isAccumulated = true)
                    mockBaseSave(accumulatedScore, saved)

                    val result = adapter.save(accumulatedScore)

                    result.scoreId shouldBe 200L
                    verify(exactly = 0) { scoreUniqueSlotJpaRepository.saveAndFlush(any()) }
                    verify(exactly = 0) { queryFactory.update(scoreUniqueSlotJpaEntity) }
                }
            }

            When("동시 요청으로 슬롯 UNIQUE 제약을 위반하면") {
                Then("SCORE_ALREADY_EXISTS 예외로 변환한다") {
                    val newScore = score(0L, ScoreStatus.PENDING, isAccumulated = false)
                    val saved = scoreEntity(101L, ScoreStatus.PENDING, isAccumulated = false)
                    mockBaseSave(newScore, saved)
                    every { scoreUniqueSlotJpaRepository.saveAndFlush(any()) } throws
                        DataIntegrityViolationException("duplicate")

                    val exception = shouldThrow<GsmcException> { adapter.save(newScore) }

                    exception.errorCode shouldBe ErrorCode.SCORE_ALREADY_EXISTS
                }
            }
        }

        Given("deleteById로 삭제할 때") {
            When("점수 ID를 전달하면") {
                Then("슬롯을 먼저 비운 뒤 점수 행을 삭제한다") {
                    val deleteClause = mockk<JPADeleteClause>()
                    every { queryFactory.delete(scoreUniqueSlotJpaEntity) } returns deleteClause
                    every { deleteClause.where(scoreUniqueSlotJpaEntity.scoreId.eq(10L)) } returns deleteClause
                    every { deleteClause.execute() } returns 1L
                    every { scoreJpaRepository.deleteById(10L) } returns Unit

                    adapter.deleteById(10L)

                    verify(exactly = 1) { deleteClause.execute() }
                    verify(exactly = 1) { scoreJpaRepository.deleteById(10L) }
                }
            }
        }
    })
