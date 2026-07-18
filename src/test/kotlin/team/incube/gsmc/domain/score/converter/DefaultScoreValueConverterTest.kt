package team.incube.gsmc.domain.score.converter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType

class DefaultScoreValueConverterTest :
    BehaviorSpec({
        val converter = DefaultScoreValueConverter()

        Given("convert") {
            When("변환이 필요 없는 카테고리에 원점수가 주어지면") {
                Then("반올림한 원점수를 그대로 반환한다") {
                    val cat =
                        Category(
                            categoryId = 1,
                            weight = 1,
                            categoryEnglishName = "Volunteer",
                            categoryKoreanName = "봉사활동",
                            categoryMaximumValue = 10,
                            isAccumulated = false,
                            evidenceType = EvidenceType.UNREQUIRED,
                            categoryType = CategoryType.VOLUNTEER,
                            calculationType = ScoreCalculationType.SCORE_BASED,
                        )

                    converter.convert(cat, 7.0) shouldBe 7
                }
            }
        }
    })
