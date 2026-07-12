package team.incube.gsmc.domain.project.adapter.out.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * DataGSM OpenAPI 공통 응답 래퍼(`{status, code, message, data}`) DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataGsmApiResponseDto<T>(
    val status: String? = null,
    val code: Int? = null,
    val message: String? = null,
    val data: T? = null,
)
