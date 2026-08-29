package team.incube.gsmc.domain.project.adapter.out.openapi

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "datagsm.openapi")
data class DataGsmOpenApiProperties(
    /** DataGSM 프로젝트 API의 기본 URL입니다. */
    val baseUrl: String,
    /** DataGSM API 호출에 사용하는 인증 키입니다. */
    val apiKey: String,
)
