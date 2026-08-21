package team.incube.gsmc.domain.evidence.adapter.web

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.evidence.Evidence
import team.incube.gsmc.domain.evidence.port.`in`.AppendEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.`in`.AppendEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.`in`.FetchEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.`in`.FetchMyEvidencesUseCase
import team.incube.gsmc.domain.evidence.port.`in`.ModifyEvidenceUseCase
import team.incube.gsmc.domain.evidence.port.`in`.RemoveEvidenceDraftUseCase
import team.incube.gsmc.domain.evidence.port.`in`.RemoveEvidenceUseCase

@Controller
class EvidenceWebAdapter(
    private val fetchMyEvidencesUseCase: FetchMyEvidencesUseCase,
    private val fetchMyEvidenceDraftUseCase: FetchMyEvidenceDraftUseCase,
    private val fetchEvidenceUseCase: FetchEvidenceUseCase,
    private val appendEvidenceUseCase: AppendEvidenceUseCase,
    private val modifyEvidenceUseCase: ModifyEvidenceUseCase,
    private val removeEvidenceUseCase: RemoveEvidenceUseCase,
    private val appendEvidenceDraftUseCase: AppendEvidenceDraftUseCase,
    private val removeEvidenceDraftUseCase: RemoveEvidenceDraftUseCase,
) {
    @QueryMapping
    fun myEvidences(): List<Evidence> = fetchMyEvidencesUseCase.execute()

    @QueryMapping
    fun myEvidenceDraft(): Evidence? = fetchMyEvidenceDraftUseCase.execute()

    @QueryMapping
    fun evidence(
        @Argument evidenceId: Long,
    ): Evidence = fetchEvidenceUseCase.execute(evidenceId)

    @MutationMapping
    fun createEvidence(
        @Argument input: CreateEvidenceInput,
    ): Evidence = appendEvidenceUseCase.execute(input.scoreId, input.title, input.content, input.fileIds)

    @MutationMapping
    fun patchEvidence(
        @Argument evidenceId: Long,
        @Argument input: PatchEvidenceInput,
    ): Evidence = modifyEvidenceUseCase.execute(evidenceId, input.title, input.content, input.fileIds)

    @MutationMapping
    fun deleteEvidence(
        @Argument evidenceId: Long,
    ): Boolean = removeEvidenceUseCase.execute(evidenceId)

    @MutationMapping
    fun createEvidenceDraft(
        @Argument input: CreateEvidenceDraftInput,
    ): Evidence = appendEvidenceDraftUseCase.execute(input.title, input.content, input.fileIds)

    @MutationMapping
    fun deleteEvidenceDraft(): Boolean = removeEvidenceDraftUseCase.execute()
}
