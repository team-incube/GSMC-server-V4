package team.incube.gsmc.global.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiry: Long,
    val refreshTokenExpiry: Long,
)
