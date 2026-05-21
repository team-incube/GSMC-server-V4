package team.incube.gsmc.domain.auth

/**
 * OAuth 인가 URL 결과
 *
 * 인가 URL 요청 시 생성된 URL과 CSRF 방지용 state 값을 담는다.
 *
 * @param url OAuth 공급자로 리다이렉트할 인가 URL
 * @param state CSRF 방지 및 요청 검증에 사용되는 무작위 값
 */
data class AuthorizationUrlResult(
    val url: String,
    val state: String,
)
