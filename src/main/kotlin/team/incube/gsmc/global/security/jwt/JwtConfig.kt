package team.incube.gsmc.global.security.jwt

import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import java.util.TimeZone
import javax.crypto.SecretKey

@Configuration
class JwtConfig(
    private val jwtProperties: JwtProperties,
) {
    @PostConstruct
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Bean
    fun jwtSigningKey(): SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
}
