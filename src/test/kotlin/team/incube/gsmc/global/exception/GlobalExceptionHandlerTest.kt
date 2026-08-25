package team.incube.gsmc.global.exception

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * [GlobalExceptionHandler]를 Spring 컨텍스트 없이 standalone MockMvc로 검증한다.
 * 예외를 던지는 것만이 목적인 [TestExceptionController]를 대상 컨트롤러로 사용한다.
 */
@RestController
private class TestExceptionController {
    @GetMapping("/test/gsmc-exception")
    fun gsmcException(): Nothing = throw GsmcException(ErrorCode.INVALID_OAUTH_STATE)

    @GetMapping("/test/data-integrity-violation")
    fun dataIntegrityViolation(): Nothing = throw DataIntegrityViolationException("Duplicate entry")

    @GetMapping("/test/generic-exception")
    fun genericException(): Nothing = throw RuntimeException("boom")
}

class GlobalExceptionHandlerTest :
    BehaviorSpec({
        val mockMvc: MockMvc =
            MockMvcBuilders
                .standaloneSetup(TestExceptionController())
                .setControllerAdvice(GlobalExceptionHandler())
                .build()

        Given("DataIntegrityViolationException이 발생했을 때") {
            When("컨트롤러에서 예외가 던져지면") {
                Then("409 상태와 DUPLICATE_RESOURCE 메시지를 응답한다") {
                    mockMvc
                        .perform(get("/test/data-integrity-violation"))
                        .andExpect(status().isConflict)
                        .andExpect(jsonPath("$.status").value(ErrorCode.DUPLICATE_RESOURCE.status.value()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_RESOURCE.message))
                }
            }
        }

        Given("GsmcException이 발생했을 때") {
            When("컨트롤러에서 예외가 던져지면") {
                Then("기존과 동일하게 errorCode에 매핑된 상태와 메시지를 응답한다") {
                    mockMvc
                        .perform(get("/test/gsmc-exception"))
                        .andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.status").value(ErrorCode.INVALID_OAUTH_STATE.status.value()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_OAUTH_STATE.message))
                }
            }
        }

        Given("그 외 일반 예외가 발생했을 때") {
            When("컨트롤러에서 예외가 던져지면") {
                Then("여전히 500 상태와 INTERNAL_SERVER_ERROR 메시지를 응답한다") {
                    mockMvc
                        .perform(get("/test/generic-exception"))
                        .andExpect(status().isInternalServerError)
                        .andExpect(jsonPath("$.status").value(ErrorCode.INTERNAL_SERVER_ERROR.status.value()))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.message))
                }
            }
        }
    })
