@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

interface RemoveProjectUseCase {
    fun execute(projectId: Long): Boolean
}
