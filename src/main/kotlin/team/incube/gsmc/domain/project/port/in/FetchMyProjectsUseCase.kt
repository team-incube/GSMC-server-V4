@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectSummary

interface FetchMyProjectsUseCase {
    fun execute(): List<ProjectSummary>
}
