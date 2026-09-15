package team.incube.gsmc.domain.file.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.transaction.support.TransactionSynchronizationManager
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.port.out.FilePersistencePort
import team.incube.gsmc.domain.file.port.out.FileStoragePort
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil

class RemoveFileServiceTest :
    BehaviorSpec({
        val filePersistencePort = mockk<FilePersistencePort>()
        val fileStoragePort = mockk<FileStoragePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = RemoveFileService(filePersistencePort, fileStoragePort, memberUtil)

        beforeEach {
            clearAllMocks()
            TransactionSynchronizationManager.initSynchronization()
        }

        afterEach { TransactionSynchronizationManager.clear() }

        val file =
            File(
                fileId = 7L,
                userId = 1L,
                fileKey = "key-7",
                fileOriginalName = "original.png",
                fileStoredName = "stored.png",
            )

        Given("파일을 삭제할 때") {
            When("존재하지 않는 파일이면") {
                Then("FILE_NOT_FOUND 예외를 던진다") {
                    every { filePersistencePort.findById(999L) } returns null

                    val exception = shouldThrow<GsmcException> { service.execute(999L) }

                    exception.errorCode shouldBe ErrorCode.FILE_NOT_FOUND
                }
            }

            When("소유자가 아니면") {
                Then("FORBIDDEN 예외를 던지고 삭제하지 않는다") {
                    every { filePersistencePort.findById(7L) } returns file
                    every { memberUtil.getCurrentUserId() } returns 999L

                    val exception = shouldThrow<GsmcException> { service.execute(7L) }

                    exception.errorCode shouldBe ErrorCode.FORBIDDEN
                    verify(exactly = 0) { filePersistencePort.deleteById(any()) }
                }
            }

            When("승인된 점수 요청에 연결되어 있으면") {
                Then("FILE_LINKED_TO_APPROVED_SCORE 예외를 던지고 삭제하지 않는다") {
                    every { filePersistencePort.findById(7L) } returns file
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { filePersistencePort.isLinkedToApprovedScore(7L) } returns true

                    val exception = shouldThrow<GsmcException> { service.execute(7L) }

                    exception.errorCode shouldBe ErrorCode.FILE_LINKED_TO_APPROVED_SCORE
                    verify(exactly = 0) { filePersistencePort.deleteById(any()) }
                }
            }

            When("소유자 본인이 승인되지 않은 파일을 삭제하면") {
                Then("DB row는 즉시 삭제하고, 스토리지 삭제는 커밋 이후로 미룬다") {
                    every { filePersistencePort.findById(7L) } returns file
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { filePersistencePort.isLinkedToApprovedScore(7L) } returns false
                    every { filePersistencePort.deleteById(7L) } just runs

                    val result = service.execute(7L)

                    result shouldBe true
                    verify(exactly = 1) { filePersistencePort.deleteById(7L) }
                    verify(exactly = 0) { fileStoragePort.deleteObject(any()) }

                    val synchronizations = TransactionSynchronizationManager.getSynchronizations()
                    synchronizations.size shouldBe 1
                }

                Then("트랜잭션이 커밋되면 스토리지 객체를 삭제한다") {
                    every { filePersistencePort.findById(7L) } returns file
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { filePersistencePort.isLinkedToApprovedScore(7L) } returns false
                    every { filePersistencePort.deleteById(7L) } just runs
                    every { fileStoragePort.deleteObject("key-7") } just runs

                    service.execute(7L)
                    TransactionSynchronizationManager.getSynchronizations().first().afterCommit()

                    verify(exactly = 1) { fileStoragePort.deleteObject("key-7") }
                }
            }

            When("커밋 후 스토리지 삭제가 실패하면") {
                Then("예외를 전파하지 않고 고아 객체 로그만 남긴다") {
                    every { filePersistencePort.findById(7L) } returns file
                    every { memberUtil.getCurrentUserId() } returns 1L
                    every { filePersistencePort.isLinkedToApprovedScore(7L) } returns false
                    every { filePersistencePort.deleteById(7L) } just runs
                    every { fileStoragePort.deleteObject("key-7") } throws RuntimeException("s3 down")

                    service.execute(7L)
                    val synchronization = TransactionSynchronizationManager.getSynchronizations().first()

                    shouldNotThrowAny { synchronization.afterCommit() }
                    verify(exactly = 1) { fileStoragePort.deleteObject("key-7") }
                }
            }
        }
    })
