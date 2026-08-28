package team.incube.gsmc.domain.project.adapter.out.openapi

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

/**
 * DataGSM 프로젝트 데이터 OpenAPI 호출용 [RestClient]를 Bean으로 등록한다.
 * `X-API-KEY` 헤더 인증을 사용하는 순수 REST API로, 기존 OAuth 전용 SDK([team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient])와는
 * 무관하다.
 */
@Configuration
class DataGsmOpenApiConfig(
    private val dataGsmOpenApiProperties: DataGsmOpenApiProperties,
) {
    /** DataGSM 프로젝트 API 호출에 사용할 인증된 REST 클라이언트를 생성합니다. */
    @Bean
    fun dataGsmOpenApiRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(dataGsmOpenApiProperties.baseUrl)
            .defaultHeader("X-API-KEY", dataGsmOpenApiProperties.apiKey)
            .build()
}
