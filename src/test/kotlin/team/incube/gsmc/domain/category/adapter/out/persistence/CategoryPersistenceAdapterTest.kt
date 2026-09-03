package team.incube.gsmc.domain.category.adapter.out.persistence

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.CategoryJpaEntity
import team.incube.gsmc.domain.category.adapter.out.persistence.entity.QCategoryJpaEntity.categoryJpaEntity
import team.incube.gsmc.domain.category.adapter.out.persistence.repository.CategoryJpaRepository

class CategoryPersistenceAdapterTest :
    BehaviorSpec({
        val queryFactory = mockk<JPAQueryFactory>()
        val categoryJpaRepository = mockk<CategoryJpaRepository>()
        val adapter = CategoryPersistenceAdapter(queryFactory, categoryJpaRepository)

        beforeEach { clearAllMocks() }

        fun entity(
            id: Long,
            categoryType: CategoryType,
            englishName: String = "Certificate",
            koreanName: String = "자격증",
        ) = CategoryJpaEntity(
            categoryId = id,
            weight = 1,
            categoryEnglishName = englishName,
            categoryKoreanName = koreanName,
            categoryMaximumValue = 5,
            isAccumulated = false,
            evidenceType = EvidenceType.FILE,
            categoryType = categoryType,
            calculationType = ScoreCalculationType.COUNT_BASED,
            conversionDivisor = 1,
        )

        Given("categoryType으로 조회할 때") {
            When("해당 타입의 카테고리가 존재하면") {
                Then("도메인 객체로 변환해 반환한다") {
                    every {
                        categoryJpaRepository.findByCategoryType(CategoryType.CERTIFICATE)
                    } returns entity(1L, CategoryType.CERTIFICATE)

                    val result = adapter.findByCategoryType(CategoryType.CERTIFICATE)

                    result?.categoryId shouldBe 1L
                    result?.categoryType shouldBe CategoryType.CERTIFICATE
                }
            }

            When("해당 타입의 카테고리가 없으면") {
                Then("null을 반환한다") {
                    every {
                        categoryJpaRepository.findByCategoryType(CategoryType.JLPT)
                    } returns null

                    adapter.findByCategoryType(CategoryType.JLPT).shouldBeNull()
                }
            }
        }

        Given("findAll을 호출할 때") {
            When("전체 카테고리를 조회하면") {
                Then("categoryId 오름차순으로 정렬된 도메인 목록을 반환한다") {
                    val query = mockk<JPAQuery<CategoryJpaEntity>>()
                    every { queryFactory.selectFrom(categoryJpaEntity) } returns query
                    every { query.orderBy(any<OrderSpecifier<*>>()) } returns query
                    every { query.fetch() } returns
                        listOf(
                            entity(1L, CategoryType.CERTIFICATE),
                            entity(2L, CategoryType.TOEIC, "TOEIC", "토익"),
                        )

                    val result = adapter.findAll()

                    result.map { it.categoryId } shouldBe listOf(1L, 2L)
                    verify(exactly = 1) { query.orderBy(categoryJpaEntity.categoryId.asc()) }
                }
            }
        }

        Given("searchByKeyword를 호출할 때") {
            When("영문명 또는 한글명에 keyword가 포함된 카테고리가 있으면") {
                Then("매칭된 도메인 목록을 반환한다") {
                    val query = mockk<JPAQuery<CategoryJpaEntity>>()
                    every { queryFactory.selectFrom(categoryJpaEntity) } returns query
                    every { query.where(any<Predicate>()) } returns query
                    every { query.orderBy(any<OrderSpecifier<*>>()) } returns query
                    every { query.fetch() } returns listOf(entity(1L, CategoryType.CERTIFICATE))

                    val result = adapter.searchByKeyword("자격")

                    result.map { it.categoryId } shouldBe listOf(1L)
                    verify(exactly = 1) { query.where(any<Predicate>()) }
                }
            }

            When("일치하는 카테고리가 없으면") {
                Then("빈 목록을 반환한다") {
                    val query = mockk<JPAQuery<CategoryJpaEntity>>()
                    every { queryFactory.selectFrom(categoryJpaEntity) } returns query
                    every { query.where(any<Predicate>()) } returns query
                    every { query.orderBy(any<OrderSpecifier<*>>()) } returns query
                    every { query.fetch() } returns emptyList()

                    val result = adapter.searchByKeyword("존재하지않음")

                    result shouldBe emptyList()
                }
            }
        }
    })
