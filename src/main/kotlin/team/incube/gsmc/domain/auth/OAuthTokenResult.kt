package team.incube.gsmc.domain.auth

/**
 * OAuth 공급자로부터 발급받은 토큰 결과
 *
 * 인가 코드 교환 후 OAuth 공급자가 반환하는 토큰 정보를 담는다.
 * 서비스 내부 JWT 발급에 사용된 후 폐기된다.
 *
 * @param accessToken OAuth 공급자 액세스 토큰
 * @param refreshToken OAuth 공급자 리프레시 토큰 (공급자에 따라 null 가능)
 * @param expiresIn 액세스 토큰 만료까지 남은 시간 (초)
 */
data class OAuthTokenResult(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
)
