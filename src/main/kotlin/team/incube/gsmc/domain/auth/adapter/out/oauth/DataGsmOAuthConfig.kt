package team.incube.gsmc.domain.auth.adapter.out.oauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient

@Configuration
class DataGsmOAuthConfig(
    private val oAuthProperties: OAuthProperties,
) {
    @Bean
    fun dataGsmOAuthClient(): DataGsmOAuthClient =
        DataGsmOAuthClient
            .builder(oAuthProperties.clientId, oAuthProperties.clientSecret)
            .build()
}
