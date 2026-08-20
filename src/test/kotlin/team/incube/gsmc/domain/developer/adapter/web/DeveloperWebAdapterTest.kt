package team.incube.gsmc.domain.developer.adapter.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberSchoolInfoUseCase

class DeveloperWebAdapterTest :
    BehaviorSpec({
        val modifyMemberSchoolInfoUseCase = mockk<ModifyMemberSchoolInfoUseCase>()
        val webAdapter =
            DeveloperWebAdapter(
                modifyMemberSchoolInfoUseCase = modifyMemberSchoolInfoUseCase,
            )

        beforeEach { clearAllMocks() }

        Given("patchMemberSchoolInfo 뮤테이션을 호출할 때") {
            When("변경할 학적정보를 담은 input을 전달하면") {
                Then("ModifyMemberSchoolInfoUseCase에 값을 그대로 위임한 결과를 반환한다") {
                    val input = PatchMemberSchoolInfoInput(memberId = 10L, grade = 2, classNumber = 3, number = 15)
                    every { modifyMemberSchoolInfoUseCase.execute(10L, 2, 3, 15) } returns true

                    val result = webAdapter.patchMemberSchoolInfo(input)

                    result shouldBe true
                    verify(exactly = 1) { modifyMemberSchoolInfoUseCase.execute(10L, 2, 3, 15) }
                }
            }
        }
    })
