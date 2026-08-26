@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.ProjectSearchResult

interface SearchProjectsUseCase {
    fun execute(
        title: String,
        page: Int,
        size: Int,
    ): ProjectSearchResult
}
