package team.incube.gsmc.domain.sheet.port.out

interface SheetStoragePort {
    fun upload(
        key: String,
        content: ByteArray,
        contentType: String,
    )

    fun createPresignedDownloadUrl(key: String): String
}
