@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectScoreAndEvidence

interface FetchMyProjectScoreAndEvidenceUseCase {
    fun execute(projectId: Long): ProjectScoreAndEvidence
}
