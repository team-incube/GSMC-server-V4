package team.incube.gsmc.domain.auth.service

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import team.incube.gsmc.domain.auth.OAuthUserInfo
import team.incube.gsmc.domain.auth.TokenResult
import team.incube.gsmc.domain.auth.port.`in`.LoginUseCase
import team.incube.gsmc.domain.auth.port.out.AuthTokenPort
import team.incube.gsmc.domain.auth.port.out.OAuthPort
import team.incube.gsmc.domain.auth.port.out.OAuthStatePersistencePort
import team.incube.gsmc.domain.auth.port.out.RefreshTokenPersistencePort
import team.incube.gsmc.domain.auth.port.out.UserPersistencePort
import team.incube.gsmc.domain.user.User
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException

/**
 * OAuth 로그인 유스케이스 구현 클래스입니다.
 * [LoginUseCase]를 구현하며, OAuth 인가 코드를 검증하고 JWT 토큰을 발급합니다.
 * 최초 로그인 시 [UserPersistencePort]를 통해 사용자를 자동으로 생성하며, 발급된 리프레시 토큰은 [RefreshTokenPersistencePort]에 저장합니다.
 */
@Port(direction = PortDirection.INBOUND)
class LoginService(
    private val oAuthPort: OAuthPort,
    private val oAuthStatePersistencePort: OAuthStatePersistencePort,
    private val userPersistencePort: UserPersistencePort,
    private val refreshTokenPersistencePort: RefreshTokenPersistencePort,
    private val authTokenPort: AuthTokenPort,
    transactionManager: PlatformTransactionManager,
) : LoginUseCase {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    /**
     * @param code OAuth 인가 코드
     * @param state CSRF 방지용 state 값
     * @param redirectUri 인가 요청 시 사용한 리다이렉트 URI
     * @return 발급된 액세스·리프레시 토큰 및 만료 정보
     * @throws GsmcException state 불일치 시 [ErrorCode.INVALID_OAUTH_STATE]
     */
    override fun execute(
        code: String,
        state: String,
        redirectUri: String,
    ): TokenResult {
        val codeVerifier =
            oAuthStatePersistencePort.findAndDelete(state)
                ?: throw GsmcException(ErrorCode.INVALID_OAUTH_STATE)

        val oAuthToken = oAuthPort.exchangeCodeForToken(code, redirectUri, codeVerifier)
        val oAuthUserInfo = oAuthPort.getUserInfo(oAuthToken.accessToken)

        return transactionTemplate.execute { persistUserAndIssueTokens(oAuthUserInfo) }
            ?: throw GsmcException(ErrorCode.INTERNAL_SERVER_ERROR)
    }

    private fun persistUserAndIssueTokens(oAuthUserInfo: OAuthUserInfo): TokenResult {
        val user =
            userPersistencePort.findByEmail(oAuthUserInfo.email)
                ?: userPersistencePort.save(
                    User(
                        userId = 0,
                        userName = oAuthUserInfo.name ?: oAuthUserInfo.email,
                        userEmail = oAuthUserInfo.email,
                        userGrade = oAuthUserInfo.grade,
                        userClassNumber = oAuthUserInfo.classNum,
                        userNumber = oAuthUserInfo.number,
                        userRole = if (oAuthUserInfo.isStudent) UserRole.STUDENT else UserRole.TEACHER,
                    ),
                )

        val accessToken = authTokenPort.generateAccessToken(user.userId, user.userRole)
        val refreshToken = authTokenPort.generateRefreshToken(user.userId)

        refreshTokenPersistencePort.save(user.userId, refreshToken)

        return TokenResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = authTokenPort.accessTokenExpiresIn,
            refreshTokenExpiresIn = authTokenPort.refreshTokenExpiresIn,
            role = user.userRole,
        )
    }
}
