package team.incube.gsmc.global.auth

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import team.incube.gsmc.domain.auth.adapter.out.persistence.repository.UserJpaRepository

@Service
class CustomUserDetailsService(
    private val userJpaRepository: UserJpaRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userJpaRepository
                .findById(username.toLong())
                .orElseThrow { UsernameNotFoundException(username) }
        return CustomUserDetails(user.userId, user.userRole)
    }
}
