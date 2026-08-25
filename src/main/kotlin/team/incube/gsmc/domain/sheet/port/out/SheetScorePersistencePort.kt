package team.incube.gsmc.domain.sheet.port.out

interface SheetScorePersistencePort {
    fun findApprovedTotalScoreByUserIds(userIds: Collection<Long>): Map<Long, Int>
}
