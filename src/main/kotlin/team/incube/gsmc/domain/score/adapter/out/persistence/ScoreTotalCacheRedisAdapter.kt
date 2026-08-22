package team.incube.gsmc.domain.score.adapter.out.persistence

import org.springframework.data.redis.core.RedisTemplate
import team.incube.gsmc.domain.score.port.out.ScoreTotalCachePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

/**
 * 반/학년 단위 총점 집계 결과의 캐싱을 담당하는 아웃바운드 어댑터 클래스입니다.
 * [ScoreTotalCachePort]를 구현하며, `userId -> 총점` 맵을 JSON으로 직렬화해 Redis에 저장한다.
 * 점수 변경 시점마다 무효화되지만, 무효화를 놓치는 경로에 대한 안전장치로 짧은 TTL을 함께 둔다.
 */
@Adapter(direction = PortDirection.OUTBOUND)
class ScoreTotalCacheRedisAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : ScoreTotalCachePort {
    companion object {
        private const val CLASS_KEY_PREFIX = "score:class-total:"
        private const val GRADE_KEY_PREFIX = "score:grade-total:"
        private const val TTL_MINUTES = 5L
    }

    override fun findClassTotals(
        userGrade: Int,
        userClassNumber: Int,
        includeApprovedOnly: Boolean,
    ): Map<Long, Int>? = find(classKey(userGrade, userClassNumber, includeApprovedOnly))

    override fun saveClassTotals(
        userGrade: Int,
        userClassNumber: Int,
        includeApprovedOnly: Boolean,
        totals: Map<Long, Int>,
    ) = save(classKey(userGrade, userClassNumber, includeApprovedOnly), totals)

    override fun evictClassTotals(
        userGrade: Int,
        userClassNumber: Int,
    ) {
        redisTemplate.delete(classKey(userGrade, userClassNumber, true))
        redisTemplate.delete(classKey(userGrade, userClassNumber, false))
    }

    override fun findGradeTotals(
        userGrade: Int,
        includeApprovedOnly: Boolean,
    ): Map<Long, Int>? = find(gradeKey(userGrade, includeApprovedOnly))

    override fun saveGradeTotals(
        userGrade: Int,
        includeApprovedOnly: Boolean,
        totals: Map<Long, Int>,
    ) = save(gradeKey(userGrade, includeApprovedOnly), totals)

    override fun evictGradeTotals(userGrade: Int) {
        redisTemplate.delete(gradeKey(userGrade, true))
        redisTemplate.delete(gradeKey(userGrade, false))
    }

    private fun find(key: String): Map<Long, Int>? =
        redisTemplate.opsForValue().get(key)?.let {
            objectMapper.readValue(it, object : TypeReference<Map<Long, Int>>() {})
        }

    private fun save(
        key: String,
        totals: Map<Long, Int>,
    ) {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(totals), TTL_MINUTES, TimeUnit.MINUTES)
    }

    private fun classKey(
        userGrade: Int,
        userClassNumber: Int,
        includeApprovedOnly: Boolean,
    ) = "$CLASS_KEY_PREFIX$userGrade:$userClassNumber:$includeApprovedOnly"

    private fun gradeKey(
        userGrade: Int,
        includeApprovedOnly: Boolean,
    ) = "$GRADE_KEY_PREFIX$userGrade:$includeApprovedOnly"
}
