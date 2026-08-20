package team.incube.gsmc.domain.member.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.member.port.`in`.FetchMemberUseCase
import team.incube.gsmc.domain.member.port.`in`.FetchMyMemberUseCase
import team.incube.gsmc.domain.member.port.`in`.ModifyMemberSchoolInfoUseCase
import team.incube.gsmc.domain.member.port.`in`.SearchMembersUseCase

/**
 * 회원 단건/본인/검색 조회 및 학적정보 변경 GraphQL Query·Mutation 리졸버입니다.
 * 각 Query/Mutation을 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class MemberWebAdapter(
    private val fetchMemberUseCase: FetchMemberUseCase,
    private val fetchMyMemberUseCase: FetchMyMemberUseCase,
    private val searchMembersUseCase: SearchMembersUseCase,
    private val modifyMemberSchoolInfoUseCase: ModifyMemberSchoolInfoUseCase,
) {
    @QueryMapping
    fun member(
        @Argument memberId: Long,
    ): MemberPayload = fetchMemberUseCase.execute(memberId).toPayload()

    @QueryMapping
    fun myMember(): MemberPayload = fetchMyMemberUseCase.execute().toPayload()

    @QueryMapping
    fun searchMembers(
        @Argument input: SearchMemberInput,
    ): SearchMemberPayload =
        searchMembersUseCase
            .execute(
                input.email,
                input.name,
                input.role,
                input.grade,
                input.classNumber,
                input.number,
                input.limit,
                input.page,
                input.sort,
            ).toPayload()

    @MutationMapping
    fun patchMemberSchoolInfo(
        @Argument input: PatchMemberSchoolInfoInput,
    ): Boolean = modifyMemberSchoolInfoUseCase.execute(input.memberId, input.grade, input.classNumber, input.number)
}
