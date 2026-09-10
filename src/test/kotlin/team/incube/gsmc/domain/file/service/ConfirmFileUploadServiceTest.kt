package team.incube.gsmc.domain.file.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.MAX_FILE_SIZE_BYTES
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

class ConfirmFileUploadServiceTest :
    BehaviorSpec({
        val filePersistencePort = mockk<FilePersistencePort>()
        val fileStoragePort = mockk<FileStoragePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = ConfirmFileUploadService(filePersistencePort, fileStoragePort, memberUtil)

        beforeEach { clearAllMocks() }

        val fileKey = "file/uuid_original.png"

        Given("파일 업로드를 확인할 때") {
            When("동일 key로 이미 confirm된 파일이 있으면") {
                Then("FILE_ALREADY_CONFIRMED 예외를 던진다") {
                    every { filePersistencePort.findByFileKey(fileKey) } returns
                        File(
                            fileId = 1L,
                            userId = 1L,
                            fileKey = fileKey,
                            fileOriginalName = "original.png",
                            fileStoredName = "original.png",
                        )

                    val exception = shouldThrow<GsmcException> { service.execute(fileKey, "original.png") }

                    exception.errorCode shouldBe ErrorCode.FILE_ALREADY_CONFIRMED
                }
            }

            When("오브젝트 스토리지에 실제 객체가 없으면") {
                Then("S3_OBJECT_NOT_FOUND 예외를 던진다") {
                    every { filePersistencePort.findByFileKey(fileKey) } returns null
                    every { fileStoragePort.getObjectSize(fileKey) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(fileKey, "original.png") }

                    exception.errorCode shouldBe ErrorCode.S3_OBJECT_NOT_FOUND
                }
            }

            When("객체 크기가 최대 허용치를 초과하면") {
                Then("INVALID_FILE_SIZE 예외를 던진다") {
                    every { filePersistencePort.findByFileKey(fileKey) } returns null
                    every { fileStoragePort.getObjectSize(fileKey) } returns MAX_FILE_SIZE_BYTES + 1

                    val exception = shouldThrow<GsmcException> { service.execute(fileKey, "original.png") }

                    exception.errorCode shouldBe ErrorCode.INVALID_FILE_SIZE
                }
            }

            When("검증을 모두 통과하면") {
                Then("현재 사용자를 소유자로 하여 미연결 상태의 파일 메타데이터를 저장한다") {
                    every { filePersistencePort.findByFileKey(fileKey) } returns null
                    every { fileStoragePort.getObjectSize(fileKey) } returns 1024L
                    every { memberUtil.getCurrentUserId() } returns 10L
                    val savedFileSlot = slot<File>()
                    every { filePersistencePort.save(capture(savedFileSlot)) } answers
                        { savedFileSlot.captured.copy(fileId = 100L) }

                    val result = service.execute(fileKey, "original.png")

                    result.fileId shouldBe 100L
                    savedFileSlot.captured.userId shouldBe 10L
                    savedFileSlot.captured.fileKey shouldBe fileKey
                    savedFileSlot.captured.fileOriginalName shouldBe "original.png"
                    savedFileSlot.captured.fileStoredName shouldBe "uuid_original.png"
                }
            }
        }
    })
