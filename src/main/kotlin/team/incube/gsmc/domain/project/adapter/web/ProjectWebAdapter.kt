package team.incube.gsmc.domain.project.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.file.port.`in`.GenerateFileAccessUrlUseCase
import team.incube.gsmc.domain.project.DataGsmProject
import team.incube.gsmc.domain.project.Project
import team.incube.gsmc.domain.project.ProjectDraft
import team.incube.gsmc.domain.project.ProjectFile
import team.incube.gsmc.domain.project.ProjectScoreAndEvidence
import team.incube.gsmc.domain.project.ProjectSearchResult
import team.incube.gsmc.domain.project.ProjectSummary
import team.incube.gsmc.domain.project.port.`in`.AppendProjectDraftUseCase
import team.incube.gsmc.domain.project.port.`in`.AppendProjectUseCase
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectDraftUseCase
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectScoreAndEvidenceUseCase
import team.incube.gsmc.domain.project.port.`in`.FetchMyProjectsUseCase
import team.incube.gsmc.domain.project.port.`in`.FetchMyWritableDataGsmProjectsUseCase
import team.incube.gsmc.domain.project.port.`in`.FetchProjectUseCase
import team.incube.gsmc.domain.project.port.`in`.ModifyProjectUseCase
import team.incube.gsmc.domain.project.port.`in`.RemoveProjectDraftUseCase
import team.incube.gsmc.domain.project.port.`in`.RemoveProjectUseCase
import team.incube.gsmc.domain.project.port.`in`.SearchProjectsUseCase

/**
 * DataGSM 프로젝트 조회 GraphQL Query 리졸버입니다.
 * Query를 대응하는 UseCase에 위임하는 것 외의 비즈니스 로직은 갖지 않습니다.
 */
@Controller
class ProjectWebAdapter(
    private val fetchMyWritableDataGsmProjectsUseCase: FetchMyWritableDataGsmProjectsUseCase,
    private val fetchMyProjectsUseCase: FetchMyProjectsUseCase,
    private val searchProjectsUseCase: SearchProjectsUseCase,
    private val fetchMyProjectDraftUseCase: FetchMyProjectDraftUseCase,
    private val fetchProjectUseCase: FetchProjectUseCase,
    private val fetchMyProjectScoreAndEvidenceUseCase: FetchMyProjectScoreAndEvidenceUseCase,
    private val appendProjectUseCase: AppendProjectUseCase,
    private val modifyProjectUseCase: ModifyProjectUseCase,
    private val removeProjectUseCase: RemoveProjectUseCase,
    private val appendProjectDraftUseCase: AppendProjectDraftUseCase,
    private val removeProjectDraftUseCase: RemoveProjectDraftUseCase,
    private val generateFileAccessUrlUseCase: GenerateFileAccessUrlUseCase,
) {
    @QueryMapping
    fun myWritableDataGsmProjects(): List<DataGsmProject> = fetchMyWritableDataGsmProjectsUseCase.execute()

    @QueryMapping
    fun myProjects(): List<ProjectSummary> = fetchMyProjectsUseCase.execute()

    @QueryMapping
    fun searchProjects(
        @Argument title: String,
        @Argument page: Int?,
        @Argument size: Int?,
    ): ProjectSearchResult = searchProjectsUseCase.execute(title, page ?: 0, size ?: 10)

    @QueryMapping
    fun myProjectDraft(): ProjectDraft? = fetchMyProjectDraftUseCase.execute()

    @QueryMapping
    fun project(
        @Argument projectId: Long,
    ): Project = fetchProjectUseCase.execute(projectId)

    @QueryMapping
    fun myProjectScoreAndEvidence(
        @Argument projectId: Long,
    ): ProjectScoreAndEvidence = fetchMyProjectScoreAndEvidenceUseCase.execute(projectId)

    @MutationMapping
    fun createProject(
        @Argument input: CreateProjectInput,
    ): Project = appendProjectUseCase.execute(input.title, input.description, input.fileIds, input.participantIds)

    @MutationMapping
    fun updateProject(
        @Argument projectId: Long,
        @Argument input: PatchProjectInput,
    ): Project =
        modifyProjectUseCase.execute(
            projectId,
            input.title,
            input.description,
            input.fileIds,
            input.participantIds,
        )

    @MutationMapping
    fun deleteProject(
        @Argument projectId: Long,
    ): Boolean = removeProjectUseCase.execute(projectId)

    @MutationMapping
    fun createProjectDraft(
        @Argument input: CreateProjectDraftInput,
    ): ProjectDraft =
        appendProjectDraftUseCase.execute(input.title, input.description, input.fileIds, input.participantIds)

    @MutationMapping
    fun deleteProjectDraft(): Boolean = removeProjectDraftUseCase.execute()

    @SchemaMapping(typeName = "ProjectFile", field = "url")
    fun projectFileUrl(projectFile: ProjectFile): String = generateFileAccessUrlUseCase.execute(projectFile.fileKey)
}
