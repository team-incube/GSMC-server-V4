package team.incube.gsmc.domain.project.adapter.out.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * `GET /v1/projects` 응답의 `data` 필드(페이지 정보 + 프로젝트 목록) DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataGsmProjectPageDto(
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val projects: List<DataGsmProjectDto> = emptyList(),
)
