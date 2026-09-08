package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.sheet.ScoreSheetRow

interface SheetGeneratorPort {
    fun generate(
        rows: List<ScoreSheetRow>,
        sheetName: String,
    ): ByteArray
}
