package team.incube.gsmc.domain.member.adapter.web

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.incube.gsmc.domain.member.MemberSearchResult
import team.incube.gsmc.domain.member.SearchMembersQuery
import team.incube.gsmc.domain.member.SortDirection
import team.incube.gsmc.domain.member.port.`in`.FetchMemberUseCase
import team.incube.gsmc.domain.member.port.`in`.FetchMyMemberUseCase
import team.incube.gsmc.domain.member.port.`in`.SearchMembersUseCase
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole

class MemberWebAdapterTest :
    BehaviorSpec({
        val fetchMemberUseCase = mockk<FetchMemberUseCase>()
        val fetchMyMemberUseCase = mockk<FetchMyMemberUseCase>()
        val searchMembersUseCase = mockk<SearchMembersUseCase>()
        val webAdapter =
            MemberWebAdapter(
                fetchMemberUseCase = fetchMemberUseCase,
                fetchMyMemberUseCase = fetchMyMemberUseCase,
                searchMembersUseCase = searchMembersUseCase,
            )

        beforeEach { clearAllMocks() }

        fun member(memberId: Long) =
            User(
                userId = memberId,
                userName = "학생$memberId",
                userEmail = "student$memberId@gsm.hs.kr",
                userGrade = 1,
                userClassNumber = 2,
                userNumber = memberId.toInt(),
                userRole = UserRole.STUDENT,
            )

        Given("member 쿼리를 호출할 때") {
            When("memberId를 전달하면") {
                Then("FetchMemberUseCase에 위임한 결과를 Payload로 변환해 반환한다") {
                    every { fetchMemberUseCase.execute(10L) } returns member(10L)

                    val result = webAdapter.member(10L)

                    result.id shouldBe 10L
                    verify(exactly = 1) { fetchMemberUseCase.execute(10L) }
                }
            }
        }

        Given("myMember 쿼리를 호출할 때") {
            When("요청하면") {
                Then("FetchMyMemberUseCase에 위임한 결과를 Payload로 변환해 반환한다") {
                    every { fetchMyMemberUseCase.execute() } returns member(1L)

                    val result = webAdapter.myMember()

                    result.id shouldBe 1L
                    verify(exactly = 1) { fetchMyMemberUseCase.execute() }
                }
            }
        }

        Given("searchMembers 쿼리를 호출할 때") {
            When("검색 조건을 담은 input을 전달하면") {
                Then("SearchMembersUseCase에 값을 그대로 위임하고 결과를 Payload로 변환해 반환한다") {
                    val input =
                        SearchMemberInput(
                            email = null,
                            name = "홍길동",
                            role = UserRole.STUDENT,
                            grade = 1,
                            classNumber = 2,
                            number = 10,
                            limit = 50,
                            page = 0,
                            sort = SortDirection.ASC,
                        )
                    val query =
                        SearchMembersQuery(
                            email = null,
                            name = "홍길동",
                            role = UserRole.STUDENT,
                            grade = 1,
                            classNumber = 2,
                            number = 10,
                            limit = 50,
                            page = 0,
                            sort = SortDirection.ASC,
                        )
                    every {
                        searchMembersUseCase.execute(query)
                    } returns MemberSearchResult(listOf(member(10L)), 1L, 1)

                    val result = webAdapter.searchMembers(input)

                    result.members.map { it.id } shouldBe listOf(10L)
                    result.totalElements shouldBe 1
                    result.totalPages shouldBe 1
                    verify(exactly = 1) {
                        searchMembersUseCase.execute(query)
                    }
                }
            }
        }
    })
