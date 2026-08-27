package team.incube.gsmc.domain.sheet.adapter.out.s3

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import team.incube.gsmc.domain.file.adapter.out.s3.S3Properties
import team.incube.gsmc.domain.sheet.port.out.SheetStoragePort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.time.Duration

private val DOWNLOAD_URL_TTL: Duration = Duration.ofMinutes(10)

@Adapter(direction = PortDirection.OUTBOUND)
class SheetS3StorageAdapter(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val s3Properties: S3Properties,
) : SheetStoragePort {
    override fun upload(
        key: String,
        content: ByteArray,
        contentType: String,
    ) {
        try {
            s3Client.putObject(
                PutObjectRequest
                    .builder()
                    .bucket(s3Properties.bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(content.size.toLong())
                    .build(),
                RequestBody.fromBytes(content),
            )
        } catch (_: Exception) {
            throw GsmcException(ErrorCode.SHEET_UPLOAD_FAILED)
        }
    }

    override fun createPresignedDownloadUrl(key: String): String =
        try {
            val getObjectRequest =
                GetObjectRequest
                    .builder()
                    .bucket(s3Properties.bucket)
                    .key(key)
                    .build()
            val presignRequest =
                GetObjectPresignRequest
                    .builder()
                    .signatureDuration(DOWNLOAD_URL_TTL)
                    .getObjectRequest(getObjectRequest)
                    .build()

            s3Presigner.presignGetObject(presignRequest).url().toString()
        } catch (_: Exception) {
            throw GsmcException(ErrorCode.SHEET_PRESIGNED_URL_FAILED)
        }
}
