package team.incube.gsmc.domain.auth

import team.incube.gsmc.domain.user.UserRole

/**
 * 인증 토큰 결과
 *
 * 로그인 또는 토큰 갱신 성공 시 반환되는 JWT 토큰 정보를 담는다.
 *
 * @param accessToken 액세스 토큰
 * @param refreshToken 리프레시 토큰
 * @param accessTokenExpiresIn 액세스 토큰 만료 시각 (Unix timestamp, ms)
 * @param refreshTokenExpiresIn 리프레시 토큰 만료 시각 (Unix timestamp, ms)
 * @param role 인증된 사용자의 권한 역할
 * @see team.incube.gsmc.domain.user.UserRole
 */
data class TokenResult(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val role: UserRole,
)
