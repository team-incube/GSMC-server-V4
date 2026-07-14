package team.incube.gsmc.domain.category.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.domain.category.port.out.CategoryPersistencePort

class SearchCategoriesServiceTest :
    BehaviorSpec({
        val categoryPersistencePort = mockk<CategoryPersistencePort>()
        val service = SearchCategoriesService(categoryPersistencePort)

        beforeEach { clearAllMocks() }

        val category =
            Category(
                categoryId = 1L,
                weight = 1,
                categoryEnglishName = "Certificate",
                categoryKoreanName = "자격증",
                categoryMaximumValue = 5,
                isAccumulated = false,
                evidenceType = EvidenceType.FILE,
                categoryType = CategoryType.CERTIFICATE,
                calculationType = ScoreCalculationType.COUNT_BASED,
            )

        Given("keyword가 주어졌을 때") {
            When("execute를 호출하면") {
                Then("영속성 포트의 searchByKeyword 결과를 반환한다") {
                    every { categoryPersistencePort.searchByKeyword("자격증") } returns listOf(category)

                    val result = service.execute("자격증")

                    result shouldBe listOf(category)
                }
            }
        }

        Given("keyword 앞뒤에 공백이 포함되었을 때") {
            When("execute를 호출하면") {
                Then("공백이 제거된 키워드로 영속성 포트의 searchByKeyword 결과를 반환한다") {
                    every { categoryPersistencePort.searchByKeyword("자격증") } returns listOf(category)

                    val result = service.execute("  자격증  ")

                    result shouldBe listOf(category)
                }
            }
        }

        Given("keyword가 null일 때") {
            When("execute를 호출하면") {
                Then("전체 카테고리를 반환한다") {
                    every { categoryPersistencePort.findAll() } returns listOf(category)

                    val result = service.execute(null)

                    result shouldBe listOf(category)
                }
            }
        }

        Given("keyword가 빈 문자열/공백일 때") {
            When("execute를 호출하면") {
                Then("전체 카테고리를 반환한다") {
                    every { categoryPersistencePort.findAll() } returns listOf(category)

                    val result = service.execute("   ")

                    result shouldBe listOf(category)
                }
            }
        }
    })
