package team.incube.gsmc.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import team.incube.gsmc.domain.auth.adapter.out.oauth.OAuthProperties
import team.incube.gsmc.global.security.jwt.JwtProperties

@Configuration
@EnableConfigurationProperties(JwtProperties::class, OAuthProperties::class)
class PropertyScanConfig
