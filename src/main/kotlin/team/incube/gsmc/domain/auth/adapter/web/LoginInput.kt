package team.incube.gsmc.domain.auth.adapter.web

data class LoginInput(
    val code: String,
    val state: String,
    val redirectUri: String,
)
