@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.evidence.port.`in`

import team.incube.gsmc.domain.evidence.Evidence

interface ModifyEvidenceUseCase {
    fun execute(
        evidenceId: Long,
        title: String?,
        content: String?,
        fileIds: List<Long>?,
    ): Evidence
}
