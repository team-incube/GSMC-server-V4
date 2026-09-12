package team.incube.gsmc.domain.category.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.mockk
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate

/**
 * [CategoryCachePersistenceAdapter]가 Spring이 자동 설정한 `ObjectMapper`로 생성되는지 검증합니다.
 *
 * Spring Boot 4.1이 제공하는 `ObjectMapper` 빈은 Jackson 3(`tools.jackson`) 하나뿐인데,
 * Jackson 2(`com.fasterxml.jackson.databind`) 타입으로 주입을 선언하면 해당 타입의 빈이 없어
 * 애플리케이션 기동 자체가 실패한다. Jackson 2 클래스는 `springdoc-openapi`의 전이 의존성으로
 * 클래스패스에 존재하므로 **컴파일과 일반 단위 테스트는 모두 통과**해, 이 오류는 실제로 컨텍스트를
 * 띄워봐야만 드러난다.
 *
 * 같은 불일치가 `9cc3afc`에서 한 번 수정된 뒤 이 어댑터에서 재발했기 때문에 회귀 방지 테스트를 둔다.
 */
class CategoryCachePersistenceAdapterBeanTest :
    BehaviorSpec({
        Given("Spring의 Jackson 자동 설정이 적용된 컨텍스트에서") {
            When("CategoryCachePersistenceAdapter를 빈으로 등록하면") {
                Then("자동 설정된 ObjectMapper로 생성에 성공한다") {
                    ApplicationContextRunner()
                        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration::class.java))
                        .withUserConfiguration(
                            CategoryCacheAdapterStubConfig::class.java,
                            CategoryCachePersistenceAdapter::class.java,
                        ).run { context ->
                            // startupFailure가 남아 있으면 주입 실패이므로, 메시지를 그대로 드러내 원인을 보이게 한다.
                            context.startupFailure?.message.shouldBeNull()
                        }
                }
            }
        }
    })

/** 어댑터 생성에만 필요한 협력 객체를 채워 넣는 테스트 전용 설정입니다. */
@Configuration
open class CategoryCacheAdapterStubConfig {
    @Bean
    open fun redisTemplate(): RedisTemplate<String, String> = mockk(relaxed = true)

    @Bean
    open fun categoryPersistenceAdapter(): CategoryPersistenceAdapter = mockk(relaxed = true)
}
