package team.incube.gsmc.global.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
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

    override fun validateToken(token: String): Boolean = runCatching { parseClaims(token) }.isSuccess

    override fun getUserIdFromToken(token: String): Long =
        runCatching { parseClaims(token).subject.toLong() }
            .getOrElse { throw GsmcException(ErrorCode.INVALID_TOKEN) }

    override fun getRoleFromToken(token: String): UserRole {
        val roleStr =
            parseClaims(token).get("role", String::class.java)
                ?: throw GsmcException(ErrorCode.INVALID_TOKEN)
        return runCatching { UserRole.valueOf(roleStr) }
            .getOrElse { throw GsmcException(ErrorCode.INVALID_TOKEN) }
    }

    override fun parseTokenClaims(token: String): Pair<Long, UserRole>? =
        runCatching {
            val claims = parseClaims(token)
            val userId = claims.subject.toLong()
            val roleStr = claims.get("role", String::class.java) ?: return@runCatching null
            val role = runCatching { UserRole.valueOf(roleStr) }.getOrNull() ?: return@runCatching null
            Pair(userId, role)
        }.getOrNull()

    fun getExpiryFromToken(token: String): Long = parseClaims(token).expiration.time

    private fun parseClaims(token: String) =
        Jwts
            .parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
