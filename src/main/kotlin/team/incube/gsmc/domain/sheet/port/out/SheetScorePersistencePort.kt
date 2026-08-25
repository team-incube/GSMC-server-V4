package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.score.Score

interface SheetScorePersistencePort {
    fun findApprovedScoresByUserIds(userIds: Collection<Long>): Map<Long, List<Score>>
}
