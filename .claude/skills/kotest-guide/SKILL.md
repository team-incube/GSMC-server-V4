---
name: kotest-guide
description: This project's Kotest + MockK test patterns — BehaviorSpec Given/When/Then structure, mockk creation, GsmcException verification. Reference when writing or reviewing test code.
---

# Kotest + MockK Test Guide

This project uses `BehaviorSpec` with `Given`/`When`/`Then` (not `DescribeSpec`).
Real example: `src/test/kotlin/team/incube/gsmc/domain/alert/service/RemoveMyAlertServiceTest.kt`

## Given-When-Then Pattern

```kotlin
class RemoveMyAlertServiceTest :
    BehaviorSpec({
        val alertPersistencePort = mockk<AlertPersistencePort>()
        val memberUtil = mockk<MemberUtil>()
        val service = RemoveMyAlertService(alertPersistencePort, memberUtil)

        beforeEach { clearAllMocks() }

        fun alert(alertId: Long, userId: Long) = Alert(
            alertId = alertId,
            userId = userId,
            scoreId = null,
            alertType = AlertType.APPROVED,
            content = "content",
            isRead = false,
            createdAt = LocalDateTime.now(),
        )

        Given("a logged-in user") {
            When("deletes their own alert") {
                Then("it is deleted successfully") {
                    every { memberUtil.getCurrentUserId() } returns 10L
                    every { alertPersistencePort.findById(1L) } returns alert(1L, 10L)
                    every { alertPersistencePort.deleteById(1L) } just runs

                    val result = service.execute(1L)

                    result shouldBe true
                    verify(exactly = 1) { alertPersistencePort.deleteById(1L) }
                }
            }
        }
    })
```

- The top-level `Given` is the state/subject, `When` is the action, `Then` is the assertion. Branch multiple scenarios with nested `When` blocks.
- Don't create a new mock for every test — create it once at the top of the spec and reset it with `beforeEach { clearAllMocks() }`.
- Mock the `port/out` interfaces or utilities in `global/util`, not real implementations (don't mock JPA directly) — dependencies like ports/utils.

## MockK Patterns

```kotlin
// creation
val port = mockk<AlertPersistencePort>()

// stubbing
every { port.findById(1L) } returns alert
every { port.deleteById(1L) } just runs

// coroutines
coEvery { port.save(any()) } returns alert

// verification
verify(exactly = 1) { port.deleteById(1L) }
verify(exactly = 0) { port.deleteById(any()) }
```

## Exception Verification — `GsmcException` + `ErrorCode`

This project throws `GsmcException(errorCode: ErrorCode)`, not `ExpectedException`. Verify the `errorCode` property as well:

```kotlin
When("trying to delete another user's alert") {
    Then("an ALERT_NOT_FOUND exception is thrown and delete is not called") {
        every { memberUtil.getCurrentUserId() } returns 10L
        every { alertPersistencePort.findById(2L) } returns alert(2L, 999L)

        val exception = shouldThrow<GsmcException> { service.execute(2L) }

        exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
        verify(exactly = 0) { alertPersistencePort.deleteById(any()) }
    }
}
```

Don't just verify the exception type — check the `errorCode` too, so the test actually catches a refactor that introduces the wrong `ErrorCode`.

## Argument Capture

```kotlin
val slot = slot<Alert>()
every { alertPersistencePort.save(capture(slot)) } returns alert
service.execute(reqDto)
slot.captured.content shouldBe "expected"
```

## Reference Files

- `src/test/kotlin/team/incube/gsmc/domain/alert/service/RemoveMyAlertServiceTest.kt`
- `src/test/kotlin/team/incube/gsmc/domain/auth/service/RefreshTokenServiceTest.kt`
- `src/test/kotlin/team/incube/gsmc/domain/developer/service/RemoveMemberServiceTest.kt`
- `src/test/kotlin/team/incube/gsmc/domain/category/service/SearchCategoriesServiceTest.kt`
