package team.incube.gsmc.domain.file.service

import io.kotest.assertions.throwables.shouldNotThrowAny
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

class RemoveSupersededFileServiceTest :
    BehaviorSpec({
        val filePersistencePort = mockk<FilePersistencePort>()
        val fileStoragePort = mockk<FileStoragePort>()
        val service = RemoveSupersededFileService(filePersistencePort, fileStoragePort)

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

        Given("밀려난 점수의 증빙 파일을 정리할 때") {
            When("execute를 호출하면") {
                Then("DB row는 즉시 삭제하고, 스토리지 삭제는 커밋 이후로 미룬다") {
                    every { filePersistencePort.deleteById(7L) } just runs
                    every { fileStoragePort.deleteObject("key-7") } just runs

                    service.execute(file)

                    verify(exactly = 1) { filePersistencePort.deleteById(7L) }
                    verify(exactly = 0) { fileStoragePort.deleteObject(any()) }

                    val synchronizations = TransactionSynchronizationManager.getSynchronizations()
                    synchronizations.size shouldBe 1
                }

                Then("트랜잭션이 커밋되면 스토리지 객체를 삭제한다") {
                    every { filePersistencePort.deleteById(7L) } just runs
                    every { fileStoragePort.deleteObject("key-7") } just runs

                    service.execute(file)
                    TransactionSynchronizationManager.getSynchronizations().first().afterCommit()

                    verify(exactly = 1) { fileStoragePort.deleteObject("key-7") }
                }
            }

            When("커밋 후 스토리지 삭제가 실패하면") {
                Then("예외를 전파하지 않고 고아 객체 로그만 남긴다") {
                    every { filePersistencePort.deleteById(7L) } just runs
                    every { fileStoragePort.deleteObject("key-7") } throws RuntimeException("s3 down")

                    service.execute(file)
                    val synchronization = TransactionSynchronizationManager.getSynchronizations().first()

                    shouldNotThrowAny { synchronization.afterCommit() }
                    verify(exactly = 1) { fileStoragePort.deleteObject("key-7") }
                }
            }
        }
    })
