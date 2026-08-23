package team.incube.gsmc.domain.score.port.out

/**
 * 반/학년 단위 총점 집계 결과의 캐싱을 추상화하는 아웃바운드 포트 인터페이스입니다.
 * 백분위 조회([team.incube.gsmc.domain.score.service.FetchMyPercentInClassService],
 * [team.incube.gsmc.domain.score.service.FetchMyPercentInGradeService])가 매 요청마다 같은
 * 반/학년 전체의 점수를 다시 로드·재계산하지 않도록, 중간 산출물인 `userId -> 총점` 맵을 캐싱한다.
 * `includeApprovedOnly` 값에 따라 결과가 달라지므로 캐시 키에 반드시 포함한다.
 */
interface ScoreTotalCachePort {
    /**
     * 캐싱된 반 전체 총점 맵을 조회한다.
     *
     * @param userGrade 학년
     * @param userClassNumber 반 번호
     * @param includeApprovedOnly 승인된 점수만 포함할지 여부
     * @return `userId -> 총점` 맵, 캐시가 없으면 null
     */
    fun findClassTotals(
        userGrade: Int,
        userClassNumber: Int,
        includeApprovedOnly: Boolean,
    ): Map<Long, Int>?

    /**
     * 반 전체 총점 맵을 캐싱한다.
     *
     * @param userGrade 학년
     * @param userClassNumber 반 번호
     * @param includeApprovedOnly 승인된 점수만 포함할지 여부
     * @param totals 캐싱할 `userId -> 총점` 맵
     */
    fun saveClassTotals(
        userGrade: Int,
        userClassNumber: Int,
        includeApprovedOnly: Boolean,
        totals: Map<Long, Int>,
    )

    /**
     * 특정 반의 캐싱된 총점 맵을 모두 무효화한다(`includeApprovedOnly` true/false 둘 다).
     *
     * @param userGrade 학년
     * @param userClassNumber 반 번호
     */
    fun evictClassTotals(
        userGrade: Int,
        userClassNumber: Int,
    )

    /**
     * 캐싱된 학년 전체 총점 맵을 조회한다.
     *
     * @param userGrade 학년
     * @param includeApprovedOnly 승인된 점수만 포함할지 여부
     * @return `userId -> 총점` 맵, 캐시가 없으면 null
     */
    fun findGradeTotals(
        userGrade: Int,
        includeApprovedOnly: Boolean,
    ): Map<Long, Int>?

    /**
     * 학년 전체 총점 맵을 캐싱한다.
     *
     * @param userGrade 학년
     * @param includeApprovedOnly 승인된 점수만 포함할지 여부
     * @param totals 캐싱할 `userId -> 총점` 맵
     */
    fun saveGradeTotals(
        userGrade: Int,
        includeApprovedOnly: Boolean,
        totals: Map<Long, Int>,
    )

    /**
     * 특정 학년의 캐싱된 총점 맵을 모두 무효화한다(`includeApprovedOnly` true/false 둘 다).
     *
     * @param userGrade 학년
     */
    fun evictGradeTotals(userGrade: Int)
}
