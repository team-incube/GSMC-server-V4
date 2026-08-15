package team.incube.gsmc.domain.file.adapter.out.s3

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import java.net.URI

class S3FileStorageAdapterTest :
    BehaviorSpec({
        val s3Client = mockk<S3Client>()
        val s3Presigner = mockk<S3Presigner>()
        val s3Properties =
            S3Properties(
                bucket = "gsmc-test-bucket",
                region = "ap-northeast-2",
                accessKey = "test-access-key",
                secretKey = "test-secret-key",
            )
        val adapter = S3FileStorageAdapter(s3Client, s3Presigner, s3Properties)

        Given("createPresignedUploadUrl로 업로드용 URL을 발급할 때") {
            When("key/contentType/fileSize를 전달하면") {
                Then("파일 크기가 서명에 포함된 요청으로 presigned URL을 발급한다") {
                    val presigned = mockk<PresignedPutObjectRequest>()
                    every { presigned.url() } returns URI.create("https://s3.example.com/upload").toURL()
                    val requestSlot = slot<software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest>()
                    every { s3Presigner.presignPutObject(capture(requestSlot)) } returns presigned

                    val result = adapter.createPresignedUploadUrl("uploads/key.png", "image/png", 1024L)

                    result.key shouldBe "uploads/key.png"
                    result.url shouldBe "https://s3.example.com/upload"
                    requestSlot.captured.putObjectRequest().bucket() shouldBe "gsmc-test-bucket"
                    requestSlot.captured.putObjectRequest().key() shouldBe "uploads/key.png"
                    requestSlot.captured.putObjectRequest().contentType() shouldBe "image/png"
                    requestSlot.captured.putObjectRequest().contentLength() shouldBe 1024L
                }
            }
        }

        Given("createPresignedDownloadUrl로 다운로드용 URL을 발급할 때") {
            When("key를 전달하면") {
                Then("해당 key에 대한 presigned URL을 발급한다") {
                    val presigned = mockk<PresignedGetObjectRequest>()
                    every { presigned.url() } returns URI.create("https://s3.example.com/download").toURL()
                    val requestSlot = slot<software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest>()
                    every { s3Presigner.presignGetObject(capture(requestSlot)) } returns presigned

                    val result = adapter.createPresignedDownloadUrl("uploads/key.png")

                    result shouldBe "https://s3.example.com/download"
                    requestSlot.captured.getObjectRequest().bucket() shouldBe "gsmc-test-bucket"
                    requestSlot.captured.getObjectRequest().key() shouldBe "uploads/key.png"
                }
            }
        }

        Given("getObjectSize로 객체 크기를 조회할 때") {
            When("해당 key의 객체가 존재하면") {
                Then("객체 크기를 반환한다") {
                    val response = mockk<HeadObjectResponse>()
                    every { response.contentLength() } returns 2048L
                    every { s3Client.headObject(any<HeadObjectRequest>()) } returns response

                    adapter.getObjectSize("uploads/key.png") shouldBe 2048L
                }
            }
            When("해당 key의 객체가 존재하지 않으면") {
                Then("null을 반환한다") {
                    every { s3Client.headObject(any<HeadObjectRequest>()) } throws
                        NoSuchKeyException.builder().build()

                    adapter.getObjectSize("uploads/missing.png").shouldBeNull()
                }
            }
        }

        Given("deleteObject로 객체를 삭제할 때") {
            When("key를 전달하면") {
                Then("해당 key의 객체 삭제를 요청한다") {
                    val requestSlot = slot<DeleteObjectRequest>()
                    every { s3Client.deleteObject(capture(requestSlot)) } returns mockk()

                    adapter.deleteObject("uploads/key.png")

                    requestSlot.captured.bucket() shouldBe "gsmc-test-bucket"
                    requestSlot.captured.key() shouldBe "uploads/key.png"
                    verify(exactly = 1) { s3Client.deleteObject(any<DeleteObjectRequest>()) }
                }
            }
        }
    })
