package team.incube.gsmc.domain.score

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import team.incube.gsmc.domain.category.Category
import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.category.EvidenceType
import team.incube.gsmc.domain.category.ScoreCalculationType
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

class ScoreValueConverterTest :
    BehaviorSpec({
        fun category(
            categoryType: CategoryType,
            categoryMaximumValue: Int,
            conversionDivisor: Int = 1,
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

                    ScoreValueConverter.convert(cat, 850.0) shouldBe 9
                    ScoreValueConverter.convert(cat, 840.0) shouldBe 8
                }
            }

            When("TOEIC 카테고리에 원점수가 주어지면(JLPT도 이 카테고리를 공유)") {
                Then("conversionDivisor로 나눈 뒤 반올림한다") {
                    val cat = category(CategoryType.TOEIC, categoryMaximumValue = 10, conversionDivisor = 100)

                    ScoreValueConverter.convert(cat, 990.0) shouldBe 10
                }
            }

            When("뉴로우스쿨 카테고리에 회고온도가 주어지면") {
                Then("conversionDivisor로 나눈 뒤 반올림한다") {
                    val cat = category(CategoryType.NEWRROW_SCHOOL, categoryMaximumValue = 5, conversionDivisor = 20)

                    ScoreValueConverter.convert(cat, 90.0) shouldBe 5
                }
            }

            When("교과성적 카테고리에 등급이 주어지면") {
                Then("(categoryMaximumValue+1)-등급으로 변환한다") {
                    val cat = category(CategoryType.ACADEMIC_GRADE, categoryMaximumValue = 9)

                    ScoreValueConverter.convert(cat, 1.0) shouldBe 9
                    ScoreValueConverter.convert(cat, 2.4) shouldBe 8
                    ScoreValueConverter.convert(cat, 9.0) shouldBe 1
                }
            }

            When("NCS 카테고리에 등급이 주어지면") {
                Then("(categoryMaximumValue+1)-등급으로 변환한다") {
                    val cat = category(CategoryType.NCS, categoryMaximumValue = 5)

                    ScoreValueConverter.convert(cat, 1.0) shouldBe 5
                    ScoreValueConverter.convert(cat, 3.6) shouldBe 2
                }
            }

            When("변환이 필요 없는 카테고리에 원점수가 주어지면") {
                Then("반올림한 원점수를 그대로 반환한다") {
                    val cat = category(CategoryType.VOLUNTEER, categoryMaximumValue = 10)

                    ScoreValueConverter.convert(cat, 7.0) shouldBe 7
                }
            }
        }

        Given("validateAcademicGradeRange") {
            When("1·2학년 학생이 1~5 범위 등급을 제출하면") {
                Then("예외가 발생하지 않는다") {
                    ScoreValueConverter.validateAcademicGradeRange(1, 5.0)
                    ScoreValueConverter.validateAcademicGradeRange(2, 1.0)
                }
            }

            When("1·2학년 학생이 5등급제 범위를 벗어난 등급을 제출하면") {
                Then("INVALID_SCORE_VALUE 예외가 발생한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            ScoreValueConverter.validateAcademicGradeRange(1, 7.0)
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_SCORE_VALUE
                }
            }

            When("3학년 학생이 1~9 범위 등급을 제출하면") {
                Then("예외가 발생하지 않는다") {
                    ScoreValueConverter.validateAcademicGradeRange(3, 9.0)
                }
            }

            When("3학년 학생이 9등급제 범위를 벗어난 등급을 제출하면") {
                Then("INVALID_SCORE_VALUE 예외가 발생한다") {
                    val exception =
                        shouldThrow<GsmcException> {
                            ScoreValueConverter.validateAcademicGradeRange(3, 10.0)
                        }

                    exception.errorCode shouldBe ErrorCode.INVALID_SCORE_VALUE
                }
            }
        }
    })
