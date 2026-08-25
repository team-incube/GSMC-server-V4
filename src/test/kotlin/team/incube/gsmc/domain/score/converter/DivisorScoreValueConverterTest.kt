package team.incube.gsmc.domain.score.converter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType

class DivisorScoreValueConverterTest :
    BehaviorSpec({
        val converter = DivisorScoreValueConverter()

        fun category(
            categoryType: CategoryType,
            categoryMaximumValue: Int,
            conversionDivisor: Int,
        ) = Category(
            categoryId = 1,
            weight = 1,
            categoryEnglishName = categoryType.name,
            categoryKoreanName = categoryType.name,
            categoryMaximumValue = categoryMaximumValue,
            isAccumulated = false,
            evidenceType = EvidenceType.FILE,
            categoryType = categoryType,
            calculationType = ScoreCalculationType.SCORE_BASED,
            conversionDivisor = conversionDivisor,
        )

        Given("convert") {
            When("TOPCIT 카테고리에 원점수가 주어지면") {
                Then("conversionDivisor로 나눈 뒤 반올림한다") {
                    val cat = category(CategoryType.TOPCIT, categoryMaximumValue = 10, conversionDivisor = 100)

                    converter.convert(cat, 850.0) shouldBe 9
                    converter.convert(cat, 840.0) shouldBe 8
                }
            }

            When("TOEIC 카테고리에 원점수가 주어지면") {
                Then("conversionDivisor로 나눈 뒤 반올림한다") {
                    val cat = category(CategoryType.TOEIC, categoryMaximumValue = 10, conversionDivisor = 100)

                    converter.convert(cat, 990.0) shouldBe 10
                }
            }

            When("뉴로우스쿨 카테고리에 회고온도가 주어지면") {
                Then("conversionDivisor로 나눈 뒤 반올림한다") {
                    val cat = category(CategoryType.NEWRROW_SCHOOL, categoryMaximumValue = 5, conversionDivisor = 20)

                    converter.convert(cat, 90.0) shouldBe 5
                }
            }
        }
    })
