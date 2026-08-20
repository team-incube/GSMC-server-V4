package team.incube.gsmc.domain.developer.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberSchoolInfoUseCase

/**
 * 회원 학적정보 변경 GraphQL Mutation 리졸버입니다.
 * 각 Mutation을 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class DeveloperWebAdapter(
    private val modifyMemberSchoolInfoUseCase: ModifyMemberSchoolInfoUseCase,
) {
    @MutationMapping
    fun patchMemberSchoolInfo(
        @Argument input: PatchMemberSchoolInfoInput,
    ): Boolean = modifyMemberSchoolInfoUseCase.execute(input.memberId, input.grade, input.classNumber, input.number)
}
