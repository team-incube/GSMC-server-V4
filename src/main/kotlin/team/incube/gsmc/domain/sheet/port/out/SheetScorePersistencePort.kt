package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.score.Score

/** 학생별 승인 점수를 조회하는 출력 포트입니다. */
interface SheetScorePersistencePort {
    /** 회원 식별자 목록에 해당하는 승인 점수를 회원별로 묶어 반환합니다. */
    fun findApprovedScoresByUserIds(userIds: Collection<Long>): Map<Long, List<Score>>
}
