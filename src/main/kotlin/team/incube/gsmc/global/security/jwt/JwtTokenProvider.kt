package team.incube.gsmc.global.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.user.UserRole
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) : AuthTokenPort {
    override val accessTokenExpiresIn: Long
        get() = jwtProperties.accessTokenExpiry

    override val refreshTokenExpiresIn: Long
        get() = jwtProperties.refreshTokenExpiry

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    override fun generateAccessToken(
        userId: Long,
        role: UserRole,
    ): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .claim("role", role.name)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiry * 1000))
            .signWith(signingKey)
            .compact()

    override fun generateRefreshToken(userId: Long): String =
        Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiry * 1000))
            .signWith(signingKey)
            .compact()

    override fun validateToken(token: String): Boolean =
        runCatching {
            parseClaims(token)
            true
        }.getOrDefault(false)

    override fun getUserIdFromToken(token: String): Long = parseClaims(token).subject.toLong()

    override fun getRoleFromToken(token: String): UserRole =
        UserRole.valueOf(
            parseClaims(token).get("role", String::class.java),
        )

    fun getExpiryFromToken(token: String): Long = parseClaims(token).expiration.time

    private fun parseClaims(token: String) =
        try {
            Jwts
                .parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: JwtException) {
            throw e
        }
}
