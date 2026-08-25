package team.incube.gsmc.global.discord

data class DiscordEmbed(
    val title: String,
    val description: String? = null,
    val color: Int,
    val fields: List<Field> = emptyList(),
    val timestamp: String? = null,
) {
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean = false,
    )

    companion object {
        const val COLOR_GREEN = 0x2ECC71
        const val COLOR_YELLOW = 0xF1C40F
        const val COLOR_ORANGE = 0xE67E22
        const val COLOR_RED = 0xE74C3C
    }
}
