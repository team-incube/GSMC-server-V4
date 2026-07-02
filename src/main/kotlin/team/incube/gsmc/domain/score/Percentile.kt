package team.incube.gsmc.domain.score

/**
 * 백분위 결과
 *
 * @param topPercentile 상위 기준 백분위 — 1등일수록 100에 가까움
 * @param bottomPercentile 하위 기준 백분위 — 1등일수록 낮은 값
 */
data class Percentile(
    val topPercentile: Int,
    val bottomPercentile: Int,
)
