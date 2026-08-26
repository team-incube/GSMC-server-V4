@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

import team.incube.gsmc.domain.project.Project

interface FetchProjectUseCase {
    fun execute(projectId: Long): Project
}
