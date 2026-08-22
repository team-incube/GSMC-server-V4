@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.evidence.port.`in`

import team.incube.gsmc.domain.evidence.Evidence

interface FetchMyEvidenceDraftUseCase {
    fun execute(): Evidence?
}
