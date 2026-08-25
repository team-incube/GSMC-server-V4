package team.incube.gsmc.domain.project.adapter.out.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import team.incube.gsmc.domain.project.DataGsmClub

/**
 * DataGSM 동아리 응답 DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataGsmClubDto(
    val id: Long,
    val name: String,
    val type: String? = null,
)

/**
 * [DataGsmClubDto]를 도메인 모델 [DataGsmClub]로 변환한다.
 */
fun DataGsmClubDto.toDomain(): DataGsmClub =
    DataGsmClub(
        clubId = id,
        clubName = name,
        clubType = type,
    )
