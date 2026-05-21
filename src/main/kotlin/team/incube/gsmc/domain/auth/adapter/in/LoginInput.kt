@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.auth.adapter.`in`

data class LoginInput(
    val code: String,
    val state: String,
    val redirectUri: String,
)
