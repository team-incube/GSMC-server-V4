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

class FetchCategoriesServiceTest :
    BehaviorSpec({
        val categoryPersistencePort = mockk<CategoryPersistencePort>()
        val service = FetchCategoriesService(categoryPersistencePort)

        beforeEach { clearAllMocks() }

        Given("전체 카테고리 목록을 조회하면") {
            When("execute를 호출하면") {
                Then("영속성 포트의 findAll 결과를 그대로 반환한다") {
                    val categories =
                        listOf(
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
                            ),
                        )
                    every { categoryPersistencePort.findAll() } returns categories

                    val result = service.execute()

                    result shouldBe categories
                }
            }
        }
    })
