---
name: kotest-guide
description: 이 프로젝트의 Kotest + MockK 테스트 패턴 — BehaviorSpec Given/When/Then 구조, mockk 생성, GsmcException 검증. 테스트 코드를 작성하거나 리뷰할 때 참조.
---

# Kotest + MockK 테스트 가이드

이 프로젝트는 `BehaviorSpec`의 `Given`/`When`/`Then`을 사용한다 (`DescribeSpec`이 아님).
실제 예시: `src/test/kotlin/team/incube/gsmc/domain/alert/service/RemoveMyAlertServiceTest.kt`

## Given-When-Then 패턴

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
            content = "내용",
            isRead = false,
            createdAt = LocalDateTime.now(),
        )

        Given("로그인한 사용자가") {
            When("본인의 알림을 삭제하면") {
                Then("정상적으로 삭제된다") {
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

- 최상위 `Given`은 상태/주체, `When`은 행동, `Then`은 검증. 중첩된 `When`으로 여러 시나리오를 분기한다.
- 매번 새 mock을 만들지 않고 스펙 최상단에서 한 번만 생성한 뒤 `beforeEach { clearAllMocks() }`로 초기화한다.
- port/util 등 의존성은 실제 구현이 아니라 `port/out` 인터페이스나 `global/util`의 유틸을 mock한다 (JPA를 직접 mock하지 않는다).

## MockK 패턴

```kotlin
// 생성
val port = mockk<AlertPersistencePort>()

// 스터빙
every { port.findById(1L) } returns alert
every { port.deleteById(1L) } just runs

// 코루틴
coEvery { port.save(any()) } returns alert

// 검증
verify(exactly = 1) { port.deleteById(1L) }
verify(exactly = 0) { port.deleteById(any()) }
```

## 예외 검증 — `GsmcException` + `ErrorCode`

이 프로젝트는 `ExpectedException`이 아니라 `GsmcException(errorCode: ErrorCode)`를 던진다. `errorCode` 프로퍼티까지 검증한다:

```kotlin
When("다른 사용자의 알림을 삭제하려 하면") {
    Then("ALERT_NOT_FOUND 예외가 발생하고 삭제는 호출되지 않는다") {
        every { memberUtil.getCurrentUserId() } returns 10L
        every { alertPersistencePort.findById(2L) } returns alert(2L, 999L)

        val exception = shouldThrow<GsmcException> { service.execute(2L) }

        exception.errorCode shouldBe ErrorCode.ALERT_NOT_FOUND
        verify(exactly = 0) { alertPersistencePort.deleteById(any()) }
    }
}
```

단순히 예외 타입만 검증하지 말고 `errorCode`까지 확인해야 잘못된 `ErrorCode`로 리팩터링했을 때 테스트가 실제로 잡아준다.

## 인자 캡처

```kotlin
val slot = slot<Alert>()
every { alertPersistencePort.save(capture(slot)) } returns alert
service.execute(reqDto)
slot.captured.content shouldBe "expected"
```

## 참고 파일 탐색

```bash
find src/test -name "*ServiceTest.kt" | head -5
find src/test -name "*Test.kt" | xargs grep -l "BehaviorSpec" | head -5
```
