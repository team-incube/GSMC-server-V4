package team.incube.gsmc.domain.sheet.adapter.out.s3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import team.incube.gsmc.domain.file.adapter.out.s3.S3Properties
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.net.URI
import java.time.Duration

class SheetS3StorageAdapterTest :
    BehaviorSpec({
        val s3Client = mockk<S3Client>()
        val s3Presigner = mockk<S3Presigner>()
        val properties = S3Properties("bucket", "ap-northeast-2", "access", "secret")
        val adapter = SheetS3StorageAdapter(s3Client, s3Presigner, properties)

        Given("Sheet ByteArray를 업로드하면") {
            Then("버킷, Key, Content-Type와 ByteArray를 S3에 전달한다") {
                val requestSlot = slot<PutObjectRequest>()
                val bodySlot = slot<RequestBody>()
                every { s3Client.putObject(capture(requestSlot), capture(bodySlot)) } returns
                    PutObjectResponse.builder().build()
                val content = byteArrayOf(1, 2, 3)

                adapter.upload(
                    "sheets/class/2/3/id.xlsx",
                    content,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                )

                requestSlot.captured.bucket() shouldBe "bucket"
                requestSlot.captured.key() shouldBe "sheets/class/2/3/id.xlsx"
                requestSlot.captured.contentType() shouldBe
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                bodySlot.captured
                    .contentStreamProvider()
                    .newStream()
                    .readBytes()
                    .toList()
                    .shouldContainExactly(1, 2, 3)
            }
        }

        Given("Sheet 다운로드 URL을 생성하면") {
            Then("객체 Key와 10분 만료시간을 사용한다") {
                val presigned = mockk<PresignedGetObjectRequest>()
                every { presigned.url() } returns URI.create("https://download.example/sheet").toURL()
                val requestSlot = slot<GetObjectPresignRequest>()
                every { s3Presigner.presignGetObject(capture(requestSlot)) } returns presigned

                adapter.createPresignedDownloadUrl("sheets/grade/2/id.xlsx") shouldBe "https://download.example/sheet"

                requestSlot.captured.getObjectRequest().bucket() shouldBe "bucket"
                requestSlot.captured.getObjectRequest().key() shouldBe "sheets/grade/2/id.xlsx"
                requestSlot.captured.signatureDuration() shouldBe Duration.ofMinutes(10)
            }
        }

        Given("S3 업로드 또는 URL 생성에 실패하면") {
            Then("내부 오류 코드로 변환한다") {
                every { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } throws RuntimeException("aws")
                shouldThrow<GsmcException> { adapter.upload("sheets/id.xlsx", byteArrayOf(), "application/test") }
                    .errorCode shouldBe ErrorCode.SHEET_UPLOAD_FAILED

                every { s3Presigner.presignGetObject(any<GetObjectPresignRequest>()) } throws RuntimeException("aws")
                shouldThrow<GsmcException> { adapter.createPresignedDownloadUrl("sheets/id.xlsx") }
                    .errorCode shouldBe ErrorCode.SHEET_PRESIGNED_URL_FAILED
            }
        }
    })
