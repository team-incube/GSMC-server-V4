@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.adapter.`in`

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import team.incube.gsmc.domain.auth.AuthorizationUrlResult
import team.incube.gsmc.domain.auth.TokenResult
import team.incube.gsmc.domain.auth.port.`in`.GetAuthorizationUrlUseCase
import team.incube.gsmc.domain.auth.port.`in`.LoginUseCase
import team.incube.gsmc.domain.auth.port.`in`.LogoutUseCase
import team.incube.gsmc.domain.auth.port.`in`.RefreshTokenUseCase

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthWebAdapter(
    private val getAuthorizationUrlUseCase: GetAuthorizationUrlUseCase,
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
) {
    @Operation(summary = "OAuth 인가 URL 조회")
    @GetMapping("/authorization-url")
    fun getAuthorizationUrl(
        @RequestParam redirectUri: String,
    ): ResponseEntity<AuthorizationUrlResult> = ResponseEntity.ok(getAuthorizationUrlUseCase.execute(redirectUri))

    @Operation(summary = "OAuth 로그인")
    @PostMapping("/login")
    fun login(
        @RequestBody input: LoginInput,
    ): ResponseEntity<TokenResult> = ResponseEntity.ok(loginUseCase.execute(input.code, input.state, input.redirectUri))

    @Operation(summary = "토큰 갱신")
    @PostMapping("/token/refresh")
    fun refreshToken(
        @RequestBody input: RefreshTokenInput,
    ): ResponseEntity<TokenResult> = ResponseEntity.ok(refreshTokenUseCase.execute(input.refreshToken))

    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        logoutUseCase.execute()
        return ResponseEntity.noContent().build()
    }
}
