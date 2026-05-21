package team.incube.gsmc.global.security.jwt

import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Configuration
class JwtConfig(
    private val jwtProperties: JwtProperties,
) {
    @Bean
    fun jwtSigningKey(): SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
}
