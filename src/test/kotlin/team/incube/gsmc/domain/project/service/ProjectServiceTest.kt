package team.incube.gsmc.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.out.EvidencePersistencePort
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.ProjectFile
import team.incube.gsmc.domain.project.ProjectParticipant
import team.incube.gsmc.domain.project.port.out.ProjectDraftPersistencePort
import team.incube.gsmc.domain.project.port.out.ProjectMemberPersistencePort
import team.incube.gsmc.domain.project.port.out.ProjectPersistencePort
import team.incube.gsmc.domain.score.Score
import team.incube.gsmc.domain.score.port.out.ScorePersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

class ProjectServiceTest :
    BehaviorSpec({
        val projectPersistencePort = mockk<ProjectPersistencePort>()
        val projectDraftPersistencePort = mockk<ProjectDraftPersistencePort>()
        val projectMemberPersistencePort = mockk<ProjectMemberPersistencePort>()
        val filePersistencePort = mockk<FilePersistencePort>()
        val scorePersistencePort = mockk<ScorePersistencePort>()
        val evidencePersistencePort = mockk<EvidencePersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val support = ProjectServiceSupport(projectMemberPersistencePort, filePersistencePort)
        val appendService = AppendProjectService(projectPersistencePort, support, memberUtil)
        val modifyService = ModifyProjectService(projectPersistencePort, support, memberUtil)
        val removeService = RemoveProjectService(projectPersistencePort, scorePersistencePort, memberUtil)
        val draftAppendService = AppendProjectDraftService(projectDraftPersistencePort, support, memberUtil)
        val draftRemoveService = RemoveProjectDraftService(projectDraftPersistencePort, memberUtil)
        val fetchMyService = FetchMyProjectsService(projectPersistencePort, memberUtil)
        val searchService = SearchProjectsService(projectPersistencePort, memberUtil)
        val scoreService =
            FetchMyProjectScoreAndEvidenceService(
                projectPersistencePort,
                scorePersistencePort,
                evidencePersistencePort,
                filePersistencePort,
                memberUtil,
            )

        beforeEach { clearAllMocks() }

        fun user(id: Long) = User(id, "사용자$id", "user$id@gsm.hs.kr", 1, 1, id.toInt(), UserRole.STUDENT)

        fun file(
            id: Long,
            ownerId: Long = 1L,
        ) = File(id, ownerId, "key-$id", "file-$id.png", "stored-$id.png")

        fun project(
            id: Long = 10L,
            ownerId: Long = 1L,
            participants: List<ProjectParticipant> = listOf(ProjectParticipant(ownerId, "사용자$ownerId")),
            files: List<ProjectFile> = emptyList(),
        ) = Project(id, ownerId, "프로젝트", "설명".repeat(100), participants, files)

        Given("프로젝트를 생성할 때") {
            When("owner가 participant 입력에 없고 중복 참여자가 있으면") {
                Then("owner를 자동 포함하고 참여자와 파일을 중복 없이 저장한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectMemberPersistencePort.findAllByUserIds(listOf(2L, 1L)) } returns
                        listOf(user(2L), user(1L))
                    every { filePersistencePort.findAllByIdIn(listOf(20L)) } returns listOf(file(20L))
                    every { projectPersistencePort.save(any()) } answers { firstArg() }

                    val result = appendService.execute("제목", "설명".repeat(150), listOf(20L, 20L), listOf(2L, 2L))

                    result.participants.map { it.id } shouldBe listOf(2L, 1L)
                    result.files.map { it.id } shouldBe listOf(20L)
                    verify { projectPersistencePort.save(match { it.ownerId == 1L }) }
                }
            }

            When("제목 또는 설명 길이가 범위를 벗어나면") {
                Then("입력 오류를 반환하고 저장하지 않는다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    shouldThrow<GsmcException> {
                        appendService.execute(
                            "",
                            "설명",
                            emptyList(),
                            emptyList(),
                        )
                    }.errorCode shouldBe
                        ErrorCode.INVALID_PROJECT_INPUT
                    verify(exactly = 0) { projectPersistencePort.save(any()) }
                }
            }

            When("존재하지 않는 참여자나 타인 소유 파일을 전달하면") {
                Then("각각 검증 오류를 반환한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectMemberPersistencePort.findAllByUserIds(listOf(1L)) } returns listOf(user(1L))
                    every { filePersistencePort.findAllByIdIn(listOf(20L)) } returns listOf(file(20L, 2L))

                    shouldThrow<GsmcException> {
                        appendService.execute("제목", "설명".repeat(150), listOf(20L), emptyList())
                    }.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }
        }

        Given("프로젝트를 수정할 때") {
            When("일부 필드만 전달하면") {
                Then("전달하지 않은 필드와 관계를 유지한다") {
                    every { projectPersistencePort.findById(10L) } returns
                        project(files = listOf(ProjectFile(20L, "file", "key")))
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectMemberPersistencePort.findAllByUserIds(listOf(1L)) } returns listOf(user(1L))
                    every { projectPersistencePort.save(any()) } answers { firstArg() }

                    val result = modifyService.execute(10L, "새 제목", null, null, null)

                    result.title shouldBe "새 제목"
                    result.description shouldBe "설명".repeat(100)
                    result.files.map { it.id } shouldBe listOf(20L)
                }
            }

            When("명시적인 빈 파일 목록을 전달하면") {
                Then("파일 관계만 해제한다") {
                    every { projectPersistencePort.findById(10L) } returns
                        project(files = listOf(ProjectFile(20L, "file", "key")))
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectMemberPersistencePort.findAllByUserIds(listOf(1L)) } returns listOf(user(1L))
                    every { filePersistencePort.findAllByIdIn(emptyList()) } returns emptyList()
                    every { projectPersistencePort.save(any()) } answers { firstArg() }

                    modifyService.execute(10L, null, null, emptyList(), null).files shouldBe emptyList()
                }
            }

            When("owner를 participant에서 제거하려 하면") {
                Then("권한 오류를 반환한다") {
                    every { projectPersistencePort.findById(10L) } returns project()
                    every { memberUtil.getCurrentUserId() } returns 1L

                    shouldThrow<GsmcException> {
                        modifyService.execute(
                            10L,
                            null,
                            null,
                            null,
                            listOf(2L),
                        )
                    }.errorCode shouldBe
                        ErrorCode.FORBIDDEN
                }
            }

            When("비소유자가 수정하면") {
                Then("권한 오류를 반환한다") {
                    every { projectPersistencePort.findById(10L) } returns project()
                    every { memberUtil.getCurrentUserId() } returns 2L

                    shouldThrow<GsmcException> {
                        modifyService.execute(
                            10L,
                            "새 제목",
                            null,
                            null,
                            null,
                        )
                    }.errorCode shouldBe
                        ErrorCode.FORBIDDEN
                }
            }
        }

        Given("내 프로젝트와 검색 결과를 조회할 때") {
            When("목록과 페이지를 요청하면") {
                Then("소유 또는 참여 프로젝트와 DB 페이지 결과를 반환한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectPersistencePort.findAllByUserId(1L) } returns listOf(project())
                    every { projectPersistencePort.findAllByTitleContaining("프로", 0, 10) } returns listOf(project())
                    every { projectPersistencePort.countByTitleContaining("프로") } returns 1L

                    fetchMyService.execute().single().ownerId shouldBe 1L
                    searchService.execute("프로", 0, 10).totalElements shouldBe 1L
                }
            }

            When("음수 페이지나 100을 초과한 크기를 요청하면") {
                Then("페이지 입력 오류를 반환한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    shouldThrow<GsmcException> { searchService.execute("프로", -1, 10) }.errorCode shouldBe
                        ErrorCode.INVALID_PAGE
                    shouldThrow<GsmcException> { searchService.execute("프로", 0, 101) }.errorCode shouldBe
                        ErrorCode.INVALID_PAGE_SIZE
                }
            }
        }

        Given("프로젝트 초안을 저장하거나 삭제할 때") {
            When("초안을 다시 저장하면") {
                Then("사용자별 초안을 upsert한다") {
                    val draft = ProjectDraft("제목", "설명", emptyList(), emptyList())
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectMemberPersistencePort.findAllByUserIds(emptyList()) } returns emptyList()
                    every { filePersistencePort.findAllByIdIn(emptyList()) } returns emptyList()
                    every { projectDraftPersistencePort.save(1L, any()) } returns draft

                    draftAppendService.execute("제목", "설명", emptyList(), emptyList()) shouldBe draft
                    verify(exactly = 1) { projectDraftPersistencePort.save(1L, any()) }
                }
            }

            When("초안 삭제를 요청하면") {
                Then("현재 사용자 초안만 삭제하고 true를 반환한다") {
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { projectDraftPersistencePort.deleteByOwnerId(1L) } just runs

                    draftRemoveService.execute() shouldBe true
                    verify { projectDraftPersistencePort.deleteByOwnerId(1L) }
                }
            }
        }

        Given("프로젝트 Score와 Evidence를 조회할 때") {
            When("현재 사용자가 참여자이고 연결된 Score가 있으면") {
                Then("Score와 Evidence 파일을 함께 반환한다") {
                    val score = mockk<Score>(relaxed = true)
                    val evidence = Evidence(50L, 1L, "제목", "내용", null, null)
                    every { projectPersistencePort.findById(10L) } returns project()
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { scorePersistencePort.findByUserIdAndProjectId(1L, 10L) } returns score
                    every { score.evidence } returns evidence
                    every { evidencePersistencePort.findById(50L) } returns evidence
                    every { filePersistencePort.findAllByEvidenceId(50L) } returns emptyList()

                    val result = scoreService.execute(10L)

                    result.score shouldBe score
                    result.evidence shouldBe listOf(evidence)
                }
            }

            When("비참여자이거나 Score가 없으면") {
                Then("각각 권한 오류와 Score 없음 오류를 반환한다") {
                    every { projectPersistencePort.findById(10L) } returns project()
                    every { memberUtil.getCurrentUserId() } returns 2L
                    shouldThrow<GsmcException> { scoreService.execute(10L) }.errorCode shouldBe ErrorCode.FORBIDDEN

                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { scorePersistencePort.findByUserIdAndProjectId(1L, 10L) } returns null
                    shouldThrow<GsmcException> { scoreService.execute(10L) }.errorCode shouldBe
                        ErrorCode.SCORE_NOT_FOUND
                }
            }
        }

        Given("프로젝트를 삭제할 때") {
            When("owner가 삭제하면") {
                Then("Score 연결만 해제하고 Project를 삭제한다") {
                    every { projectPersistencePort.findById(10L) } returns project()
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { scorePersistencePort.unlinkProject(10L) } just runs
                    every { projectPersistencePort.deleteById(10L) } just runs

                    removeService.execute(10L) shouldBe true
                    verify { scorePersistencePort.unlinkProject(10L) }
                    verify { projectPersistencePort.deleteById(10L) }
                }
            }
        }
    })
