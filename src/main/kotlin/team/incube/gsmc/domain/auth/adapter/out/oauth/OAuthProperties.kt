package team.incube.gsmc.domain.auth.adapter.out.oauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.oauth")
data class OAuthProperties(
    val clientId: String,
    val clientSecret: String,
)
