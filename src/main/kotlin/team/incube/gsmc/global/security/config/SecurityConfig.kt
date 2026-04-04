package team.incube.gsmc.global.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/*
TODO: 인증/인가 구현 또는 보안 관련 설정 시 해당 클래스를 무조건 변경하여주세요.
 */
@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
        return http.build()
    }
}
