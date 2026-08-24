package team.incube.gsmc.domain.developer.adapter.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberRoleUseCase
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberSchoolInfoUseCase
import team.incube.gsmc.domain.user.UserRole

class DeveloperWebAdapterTest :
    BehaviorSpec({
        val modifyMemberSchoolInfoUseCase = mockk<ModifyMemberSchoolInfoUseCase>()
        val modifyMemberRoleUseCase = mockk<ModifyMemberRoleUseCase>()
        val webAdapter =
            DeveloperWebAdapter(
                modifyMemberSchoolInfoUseCase = modifyMemberSchoolInfoUseCase,
                modifyMemberRoleUseCase = modifyMemberRoleUseCase,
            )

        beforeEach { clearAllMocks() }

        Given("patchMemberRole 뮤테이션을 호출할 때") {
            When("변경할 역할을 담은 input을 전달하면") {
                Then("ModifyMemberRoleUseCase에 값을 그대로 위임한 결과를 반환한다") {
                    val input = PatchMemberRoleInput(email = "student@gsm.hs.kr", role = UserRole.TEACHER)
                    every { modifyMemberRoleUseCase.execute("student@gsm.hs.kr", UserRole.TEACHER) } returns true

                    val result = webAdapter.patchMemberRole(input)

                    result shouldBe true
                    verify(exactly = 1) { modifyMemberRoleUseCase.execute("student@gsm.hs.kr", UserRole.TEACHER) }
                }
            }
        }
    })
