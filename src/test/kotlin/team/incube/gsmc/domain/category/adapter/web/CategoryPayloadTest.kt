package team.incube.gsmc.domain.category.adapter.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType

class CategoryPayloadTest :
    BehaviorSpec({
        Given("Category 도메인 객체가") {
            val category =
                Category(
                    categoryId = 1L,
                    weight = 3,
                    categoryEnglishName = "Certificate",
                    categoryKoreanName = "자격증",
                    categoryMaximumValue = 5,
                    isAccumulated = false,
                    evidenceType = EvidenceType.FILE,
                    categoryType = CategoryType.CERTIFICATE,
                    calculationType = ScoreCalculationType.COUNT_BASED,
                )

            When("toPayload()로 변환하면") {
                Then("필드명이 바뀐 CategoryPayload로 정확히 매핑된다") {
                    val payload = category.toPayload()

                    payload shouldBe
                        CategoryPayload(
                            categoryType = CategoryType.CERTIFICATE,
                            englishName = "Certificate",
                            koreanName = "자격증",
                            weight = 3,
                            maxRecordCount = 5,
                            evidenceType = EvidenceType.FILE,
                            calculationType = ScoreCalculationType.COUNT_BASED,
                        )
                }
            }
        }
    })
