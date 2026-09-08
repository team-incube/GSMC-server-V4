package team.incube.gsmc.domain.developer.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberRoleUseCase
import team.incube.gsmc.domain.developer.port.`in`.ModifyMemberSchoolInfoUseCase
import team.incube.gsmc.domain.developer.port.`in`.RemoveMemberUseCase

/**
 * 개발자 전용 GraphQL Mutation 리졸버입니다.
 * 각 Mutation을 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class DeveloperWebAdapter(
    private val modifyMemberSchoolInfoUseCase: ModifyMemberSchoolInfoUseCase,
    private val modifyMemberRoleUseCase: ModifyMemberRoleUseCase,
    private val removeMemberUseCase: RemoveMemberUseCase,
) {
    @MutationMapping
    fun patchMemberSchoolInfo(
        @Argument input: PatchMemberSchoolInfoInput,
    ): Boolean = modifyMemberSchoolInfoUseCase.execute(input.memberId, input.grade, input.classNumber, input.number)

    @MutationMapping
    fun patchMemberRole(
        @Argument input: PatchMemberRoleInput,
    ): Boolean = modifyMemberRoleUseCase.execute(input.email, input.role)

    @MutationMapping
    fun deleteMember(
        @Argument memberId: Long,
    ): Boolean = removeMemberUseCase.execute(memberId)
}
