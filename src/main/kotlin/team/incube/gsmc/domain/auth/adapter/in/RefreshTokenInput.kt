@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.adapter.`in`

/**
 * 토큰 갱신 요청 입력 데이터 클래스입니다.
 * GraphQL 뮤테이션의 [AuthWebAdapter] 진입점에서 수신하는 인바운드 DTO입니다.
 *
 * @param refreshToken 재발급에 사용할 리프레시 토큰
 */
data class RefreshTokenInput(
    val refreshToken: String,
)
