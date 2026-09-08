package team.incube.gsmc.domain.project.adapter.out.openapi

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "datagsm.openapi")
data class DataGsmOpenApiProperties(
    /** DataGSM 프로젝트 API의 기본 URL입니다. */
    val baseUrl: String,
    /** DataGSM API 호출에 사용하는 인증 키입니다. */
    val apiKey: String,
    val connectTimeout: Duration = Duration.ofSeconds(3),
    val readTimeout: Duration = Duration.ofSeconds(5),
)
