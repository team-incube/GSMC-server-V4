package team.incube.gsmc.domain.score.service

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import team.incube.gsmc.domain.score.port.out.MemberPersistencePort
import team.incube.gsmc.domain.score.port.out.ScoreTotalCachePort
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

/**
 * 점수 상태가 바뀔 때 해당 학생이 속한 반/학년의 캐싱된 총점 맵을 무효화하는 헬퍼 클래스입니다.
 * 점수를 추가/승인/거절/삭제하는 모든 서비스에서 공통으로 사용된다. [MemberPersistencePort]에는
 * grade/classNumber가 없는 [team.incube.gsmc.global.util.MemberUtil] 대신, 대상 학생을 다시
 * 조회해 학년/반을 알아낸다. 포트가 아닌 순수 협력 객체로, 여러 서비스에 그대로 주입된다.
 *
 * 쓰기가 짧은 시간에 몰리면(예: 교사의 일괄 승인, 마감 직전 제출 폭주) 매번 즉시 무효화할 경우 같은
 * 반/학년 캐시가 계속 지워졌다 다시 채워지길 반복해 캐싱 효과가 사실상 사라진다. 이를 막기 위해
 * [DEBOUNCE_DELAY]만큼 실제 무효화를 지연시키고, 그 지연 창 안에 도착한 추가 무효화 요청은 새로
 * 예약하지 않고 이미 예약된 한 번의 무효화에 묶는다(디바운스). [TaskScheduler]는 단일 인스턴스
 * 기준으로 동작하므로, 이 서비스가 여러 인스턴스로 스케일아웃되면 인스턴스별로 최대 1회씩 무효화될
 * 수 있으나 여전히 요청당 1회보다는 훨씬 적고, 5분 TTL이 최종 안전장치로 남아있다.
 */
@Component
class ScoreTotalCacheInvalidator(
    private val memberPersistencePort: MemberPersistencePort,
    private val scoreTotalCachePort: ScoreTotalCachePort,
    private val taskScheduler: TaskScheduler,
) {
    companion object {
        private val DEBOUNCE_DELAY: Duration = Duration.ofSeconds(5)
    }

    private val pendingGradeEvictions = ConcurrentHashMap<Int, ScheduledFuture<*>>()
    private val pendingClassEvictions = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun invalidate(userId: Long) {
        val member = memberPersistencePort.findByUserId(userId) ?: return
        val userGrade = member.userGrade ?: return

        debounceGradeEviction(userGrade)
        member.userClassNumber?.let { debounceClassEviction(userGrade, it) }
    }

    private fun debounceGradeEviction(userGrade: Int) {
        pendingGradeEvictions.computeIfAbsent(userGrade) {
            taskScheduler.schedule(
                {
                    scoreTotalCachePort.evictGradeTotals(userGrade)
                    pendingGradeEvictions.remove(userGrade)
                },
                Instant.now().plus(DEBOUNCE_DELAY),
            )
        }
    }

    private fun debounceClassEviction(
        userGrade: Int,
        userClassNumber: Int,
    ) {
        val key = classKey(userGrade, userClassNumber)
        pendingClassEvictions.computeIfAbsent(key) {
            taskScheduler.schedule(
                {
                    scoreTotalCachePort.evictClassTotals(userGrade, userClassNumber)
                    pendingClassEvictions.remove(key)
                },
                Instant.now().plus(DEBOUNCE_DELAY),
            )
        }
    }

    private fun classKey(
        userGrade: Int,
        userClassNumber: Int,
    ) = "$userGrade:$userClassNumber"
}
