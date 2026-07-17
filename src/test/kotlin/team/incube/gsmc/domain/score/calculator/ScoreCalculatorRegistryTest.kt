package team.incube.gsmc.domain.score.calculator

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import team.incube.gsmc.domain.category.CategoryType

class ScoreCalculatorRegistryTest :
    BehaviorSpec({
        Given("resolve") {
            When("TOEIC 또는 JLPT가 주어지면") {
                Then("ToeicScoreCalculator를 반환한다") {
                    ScoreCalculatorRegistry.resolve(CategoryType.TOEIC).shouldBeInstanceOf<ToeicScoreCalculator>()
                    ScoreCalculatorRegistry.resolve(CategoryType.JLPT).shouldBeInstanceOf<ToeicScoreCalculator>()
                }
            }

            When("그 외 카테고리 타입이 주어지면") {
                Then("DefaultScoreCalculator를 반환한다") {
                    ScoreCalculatorRegistry
                        .resolve(
                            CategoryType.CERTIFICATE,
                        ).shouldBeInstanceOf<DefaultScoreCalculator>()
                    ScoreCalculatorRegistry
                        .resolve(
                            CategoryType.ACADEMIC_GRADE,
                        ).shouldBeInstanceOf<DefaultScoreCalculator>()
                    ScoreCalculatorRegistry
                        .resolve(
                            CategoryType.TOEIC_ACADEMY,
                        ).shouldBeInstanceOf<DefaultScoreCalculator>()
                }
            }

            When("같은 카테고리 타입으로 여러 번 호출하면") {
                Then("항상 같은 인스턴스를 반환한다") {
                    ScoreCalculatorRegistry.resolve(CategoryType.TOEIC) shouldBe
                        ScoreCalculatorRegistry.resolve(CategoryType.TOEIC)
                }
            }
        }
    })
