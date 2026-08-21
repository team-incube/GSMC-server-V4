@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.evidence.port.`in`

interface RemoveEvidenceUseCase {
    fun execute(evidenceId: Long): Boolean
}
