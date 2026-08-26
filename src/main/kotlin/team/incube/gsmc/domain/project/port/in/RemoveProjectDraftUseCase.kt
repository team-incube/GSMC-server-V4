@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.project.port.`in`

interface RemoveProjectDraftUseCase {
    fun execute(): Boolean
}
