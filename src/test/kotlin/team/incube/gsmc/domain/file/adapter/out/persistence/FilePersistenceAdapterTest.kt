package team.incube.gsmc.domain.file.adapter.out.persistence

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import team.incube.gsmc.domain.evidence.adapter.out.persistence.entity.EvidenceJpaEntity
import team.incube.gsmc.domain.file.File
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.file.adapter.out.persistence.repository.FileJpaRepository
import team.incube.gsmc.domain.score.adapter.out.persistence.entity.ScoreJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity
import java.util.Optional

class FilePersistenceAdapterTest :
    BehaviorSpec({
        val fileJpaRepository = mockk<FileJpaRepository>()
        val entityManager = mockk<EntityManager>()
        val adapter = FilePersistenceAdapter(fileJpaRepository, entityManager)

        beforeEach { clearAllMocks() }

        fun user(userId: Long) =
            mockk<UserJpaEntity> {
                every { this@mockk.userId } returns userId
            }

        fun entity(
            fileId: Long,
            userId: Long = 1L,
            score: ScoreJpaEntity? = null,
            evidence: EvidenceJpaEntity? = null,
            fileKey: String = "key-$fileId",
        ) = FileJpaEntity(
            fileId = fileId,
            user = user(userId),
            score = score,
            evidence = evidence,
            fileKey = fileKey,
            fileOriginalName = "original-$fileId.png",
            fileStoredName = "stored-$fileId.png",
        )

        Given("findById로 조회할 때") {
            When("해당 ID의 파일이 존재하면") {
                Then("도메인 객체로 변환해 반환한다") {
                    every { fileJpaRepository.findById(10L) } returns Optional.of(entity(10L))

                    val result = adapter.findById(10L)

                    result?.fileId shouldBe 10L
                    result?.fileKey shouldBe "key-10"
                }
            }
            When("해당 ID의 파일이 존재하지 않으면") {
                Then("null을 반환한다") {
                    every { fileJpaRepository.findById(999L) } returns Optional.empty()

                    adapter.findById(999L).shouldBeNull()
                }
            }
        }

        Given("findByFileKey로 조회할 때") {
            When("해당 key의 파일이 존재하면") {
                Then("도메인 객체로 변환해 반환한다") {
                    every { fileJpaRepository.findByFileKey("key-10") } returns entity(10L, fileKey = "key-10")

                    val result = adapter.findByFileKey("key-10")

                    result?.fileId shouldBe 10L
                }
            }
            When("해당 key의 파일이 존재하지 않으면") {
                Then("null을 반환한다") {
                    every { fileJpaRepository.findByFileKey("unknown") } returns null

                    adapter.findByFileKey("unknown").shouldBeNull()
                }
            }
        }

        Given("findAllByEvidenceId로 조회할 때") {
            When("연결된 파일이 여러 건 있으면") {
                Then("모두 도메인 객체 목록으로 변환해 반환한다") {
                    every { fileJpaRepository.findAllByEvidenceEvidenceId(5L) } returns
                        listOf(entity(1L), entity(2L))

                    val result = adapter.findAllByEvidenceId(5L)

                    result.map { it.fileId } shouldBe listOf(1L, 2L)
                }
            }
        }

        Given("findAllByUserId로 조회할 때") {
            When("업로드한 파일이 여러 건 있으면") {
                Then("모두 도메인 객체 목록으로 변환해 반환한다") {
                    every { fileJpaRepository.findAllByUserUserId(1L) } returns
                        listOf(entity(1L), entity(2L))

                    val result = adapter.findAllByUserId(1L)

                    result.map { it.fileId } shouldBe listOf(1L, 2L)
                }
            }
        }

        Given("save로 저장할 때") {
            When("신규 File 도메인 객체를 저장하면") {
                Then("user 참조를 조회해 엔티티로 변환한 뒤 저장하고 도메인 객체로 반환한다") {
                    val newFile =
                        File(
                            fileId = 0,
                            userId = 1L,
                            fileKey = "new-key",
                            fileOriginalName = "original.png",
                            fileStoredName = "stored.png",
                        )
                    val userRef = user(1L)
                    every { entityManager.getReference(UserJpaEntity::class.java, 1L) } returns userRef
                    val savedSlot = slot<FileJpaEntity>()
                    every { fileJpaRepository.save(capture(savedSlot)) } answers
                        {
                            entity(100L, fileKey = savedSlot.captured.fileKey)
                        }

                    val result = adapter.save(newFile)

                    result.fileId shouldBe 100L
                    savedSlot.captured.fileKey shouldBe "new-key"
                    savedSlot.captured.score shouldBe null
                    savedSlot.captured.evidence shouldBe null
                    verify(exactly = 1) { entityManager.getReference(UserJpaEntity::class.java, 1L) }
                }
            }
        }

        Given("deleteById로 삭제할 때") {
            When("파일 ID를 전달하면") {
                Then("리포지토리에 삭제를 위임한다") {
                    every { fileJpaRepository.deleteById(10L) } returns Unit

                    adapter.deleteById(10L)

                    verify(exactly = 1) { fileJpaRepository.deleteById(10L) }
                }
            }
        }

        Given("linkToEvidence로 근거 자료를 연결할 때") {
            When("대상 파일이 존재하면") {
                Then("evidence 참조를 채운 새 엔티티로 저장한다") {
                    val target = entity(10L)
                    val evidenceRef = mockk<EvidenceJpaEntity>()
                    every { fileJpaRepository.findById(10L) } returns Optional.of(target)
                    every { entityManager.getReference(EvidenceJpaEntity::class.java, 5L) } returns evidenceRef
                    val savedSlot = slot<FileJpaEntity>()
                    every { fileJpaRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

                    adapter.linkToEvidence(10L, 5L)

                    savedSlot.captured.fileId shouldBe 10L
                    savedSlot.captured.evidence shouldBe evidenceRef
                }
            }
            When("대상 파일이 존재하지 않으면") {
                Then("아무 것도 하지 않는다") {
                    every { fileJpaRepository.findById(999L) } returns Optional.empty()

                    adapter.linkToEvidence(999L, 5L)

                    verify(exactly = 0) { fileJpaRepository.save(any()) }
                    verify(exactly = 0) { entityManager.getReference(EvidenceJpaEntity::class.java, any()) }
                }
            }
        }

        Given("unlinkFromEvidence로 근거 자료 연결을 해제할 때") {
            When("대상 파일이 존재하면") {
                Then("evidence를 null로 비운 새 엔티티로 저장한다") {
                    val evidenceRef = mockk<EvidenceJpaEntity>()
                    val target = entity(10L, evidence = evidenceRef)
                    every { fileJpaRepository.findById(10L) } returns Optional.of(target)
                    val savedSlot = slot<FileJpaEntity>()
                    every { fileJpaRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

                    adapter.unlinkFromEvidence(10L)

                    savedSlot.captured.evidence shouldBe null
                }
            }
            When("대상 파일이 존재하지 않으면") {
                Then("아무 것도 하지 않는다") {
                    every { fileJpaRepository.findById(999L) } returns Optional.empty()

                    adapter.unlinkFromEvidence(999L)

                    verify(exactly = 0) { fileJpaRepository.save(any()) }
                }
            }
        }

        Given("linkToScore로 점수 요청을 연결할 때") {
            When("대상 파일이 존재하면") {
                Then("score 참조를 채운 새 엔티티로 저장한다") {
                    val target = entity(10L)
                    val scoreRef = mockk<ScoreJpaEntity>()
                    every { fileJpaRepository.findById(10L) } returns Optional.of(target)
                    every { entityManager.getReference(ScoreJpaEntity::class.java, 7L) } returns scoreRef
                    val savedSlot = slot<FileJpaEntity>()
                    every { fileJpaRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

                    adapter.linkToScore(10L, 7L)

                    savedSlot.captured.score shouldBe scoreRef
                }
            }
            When("대상 파일이 존재하지 않으면") {
                Then("아무 것도 하지 않는다") {
                    every { fileJpaRepository.findById(999L) } returns Optional.empty()

                    adapter.linkToScore(999L, 7L)

                    verify(exactly = 0) { fileJpaRepository.save(any()) }
                    verify(exactly = 0) { entityManager.getReference(ScoreJpaEntity::class.java, any()) }
                }
            }
        }

        Given("unlinkFromScore로 점수 요청 연결을 해제할 때") {
            When("대상 파일이 존재하면") {
                Then("score를 null로 비운 새 엔티티로 저장한다") {
                    val scoreRef = mockk<ScoreJpaEntity>()
                    val target = entity(10L, score = scoreRef)
                    every { fileJpaRepository.findById(10L) } returns Optional.of(target)
                    val savedSlot = slot<FileJpaEntity>()
                    every { fileJpaRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

                    adapter.unlinkFromScore(10L)

                    savedSlot.captured.score shouldBe null
                }
            }
            When("대상 파일이 존재하지 않으면") {
                Then("아무 것도 하지 않는다") {
                    every { fileJpaRepository.findById(999L) } returns Optional.empty()

                    adapter.unlinkFromScore(999L)

                    verify(exactly = 0) { fileJpaRepository.save(any()) }
                }
            }
        }
    })
