@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.adapter.`in`

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import team.incube.gsmc.domain.auth.AuthorizationUrlResult
import team.incube.gsmc.domain.auth.TokenResult
import team.incube.gsmc.domain.auth.port.`in`.GetAuthorizationUrlUseCase
import team.incube.gsmc.domain.auth.port.`in`.LoginUseCase
import team.incube.gsmc.domain.auth.port.`in`.LogoutUseCase
import team.incube.gsmc.domain.auth.port.`in`.RefreshTokenUseCase
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

/**
 * 인증 관련 GraphQL 요청을 처리하는 인바운드 어댑터 클래스입니다.
 * [GetAuthorizationUrlUseCase], [LoginUseCase], [RefreshTokenUseCase], [LogoutUseCase] 유스케이스에 요청을 위임합니다.
 * GraphQL 스키마의 인증 관련 Query/Mutation을 외부 클라이언트로부터 수신하는 진입점 역할을 합니다.
 */
@Controller
class AuthWebAdapter(
    private val getAuthorizationUrlUseCase: GetAuthorizationUrlUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) {
    /**
     * OAuth 인가 URL을 생성하여 반환한다.
     *
     * @param redirectUri OAuth 인증 완료 후 리다이렉트될 URI
     * @return 인가 URL 및 state 값
     */
    @QueryMapping
    fun getAuthorizationUrl(
        @Argument redirectUri: String,
    ): AuthorizationUrlResult = getAuthorizationUrlUseCase.execute(redirectUri)

    /**
     * OAuth 인가 코드로 로그인하여 JWT 토큰을 발급한다.
     *
     * @param input 인가 코드, state, 리다이렉트 URI
     * @return 발급된 액세스·리프레시 토큰 및 만료 정보
     */
    @MutationMapping
    fun login(
        @Argument input: LoginInput,
    ): TokenResult = loginUseCase.execute(input.code, input.state, input.redirectUri)

    /**
     * 리프레시 토큰으로 새 액세스 토큰을 발급한다.
     *
     * @param input 리프레시 토큰
     * @return 갱신된 토큰 정보
     */
    @MutationMapping
    fun refreshToken(
        @Argument input: RefreshTokenInput,
    ): TokenResult = refreshTokenUseCase.execute(input.refreshToken)

    /**
     * 현재 인증된 사용자를 로그아웃 처리한다.
     *
     * @return 로그아웃 성공 여부
     */
    @MutationMapping
    fun logout(): Boolean {
        val userId =
            SecurityContextHolder.getContext().authentication?.principal as? Long
                ?: throw GsmcException(ErrorCode.INVALID_TOKEN)
        logoutUseCase.execute(userId)
        return true
    }
}
