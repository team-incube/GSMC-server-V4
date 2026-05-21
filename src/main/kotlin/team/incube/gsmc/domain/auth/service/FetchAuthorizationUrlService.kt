package team.incube.gsmc.domain.auth.service

import team.incube.gsmc.domain.auth.AuthorizationUrlResult
import team.incube.gsmc.domain.auth.port.`in`.GetAuthorizationUrlUseCase
import team.incube.gsmc.domain.auth.port.out.OAuthPort
import team.incube.gsmc.domain.auth.port.out.OAuthStatePersistencePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import java.util.UUID

/**
 * OAuth 인가 URL 조회 유스케이스 구현 클래스입니다.
 * [GetAuthorizationUrlUseCase]를 구현하며, 무작위 state를 생성하고 PKCE codeVerifier를 Redis에 저장한 뒤 인가 URL을 반환합니다.
 * URL 생성은 [OAuthPort]에, state/codeVerifier 저장은 [OAuthStatePersistencePort]에 위임합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchAuthorizationUrlService(
    private val oAuthPort: OAuthPort,
    private val oAuthStatePersistencePort: OAuthStatePersistencePort,
) : GetAuthorizationUrlUseCase {
    /**
     * @param redirectUri OAuth 인증 완료 후 리다이렉트될 URI
     * @return 인가 URL 및 state 값
     */
    override fun execute(redirectUri: String): AuthorizationUrlResult {
        val state = UUID.randomUUID().toString()
        val (url, codeVerifier) = oAuthPort.createAuthorizationUrl(redirectUri, state)
        oAuthStatePersistencePort.save(state, codeVerifier)
        return AuthorizationUrlResult(url = url, state = state)
    }
}
