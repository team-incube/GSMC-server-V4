package team.incube.gsmc.domain.sheet.adapter.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.sheet.port.`in`.FetchClassScoreSheetUseCase
import team.incube.gsmc.domain.sheet.port.`in`.FetchGradeScoreSheetUseCase

class SheetWebAdapterTest :
    BehaviorSpec({
        val classUseCase = mockk<FetchClassScoreSheetUseCase>()
        val gradeUseCase = mockk<FetchGradeScoreSheetUseCase>()
        val adapter = SheetWebAdapter(classUseCase, gradeUseCase)

        Given("학급 Sheet Query가 호출되면") {
            Then("인자를 UseCase에 전달하고 URL을 반환한다") {
                every { classUseCase.execute(2, 3) } returns "https://download.example/class"

                adapter.classScoreSheetUrl(2, 3) shouldBe "https://download.example/class"
                verify(exactly = 1) { classUseCase.execute(2, 3) }
            }
        }

        Given("학년 Sheet Query가 호출되면") {
            Then("인자를 UseCase에 전달하고 URL을 반환한다") {
                every { gradeUseCase.execute(2) } returns "https://download.example/grade"

                adapter.gradeScoreSheetUrl(2) shouldBe "https://download.example/grade"
                verify(exactly = 1) { gradeUseCase.execute(2) }
            }
        }
    })
