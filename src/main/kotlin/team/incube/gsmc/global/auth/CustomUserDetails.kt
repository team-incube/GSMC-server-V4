package team.incube.gsmc.global.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import team.incube.gsmc.domain.user.UserRole

class CustomUserDetails(
    val userId: Long,
    val userRole: UserRole,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${userRole.name}"))

    override fun getPassword(): String = ""

    override fun getUsername(): String = userId.toString()
}
