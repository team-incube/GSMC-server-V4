@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.adapter.`in`

/**
 * OAuth 로그인 요청 입력 데이터 클래스입니다.
 * GraphQL 뮤테이션의 [AuthWebAdapter] 진입점에서 수신하는 인바운드 DTO입니다.
 *
 * @param code OAuth 인가 코드
 * @param state CSRF 방지용 state 값
 * @param redirectUri 리다이렉트 URI
 */
data class LoginInput(
    val code: String,
    val state: String,
    val redirectUri: String,
)
