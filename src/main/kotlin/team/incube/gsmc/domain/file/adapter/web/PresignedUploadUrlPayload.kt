package team.incube.gsmc.domain.file.adapter.web

import team.incube.gsmc.domain.file.PresignedUpload

data class PresignedUploadUrlPayload(
    val presignedUrl: String,
    val fileKey: String,
    val expiresAt: String,
)

fun PresignedUpload.toPayload(): PresignedUploadUrlPayload =
    PresignedUploadUrlPayload(
        presignedUrl = url,
        fileKey = key,
        expiresAt = expiresAt.toString(),
    )
