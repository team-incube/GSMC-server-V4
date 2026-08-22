@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.evidence.port.`in`

import team.incube.gsmc.domain.evidence.Evidence

interface AppendEvidenceUseCase {
    fun execute(
        scoreId: Long,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence
}
