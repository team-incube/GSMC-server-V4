package team.incube.gsmc.domain.file.adapter.web

data class CreatePresignedUploadUrlInput(
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
)
