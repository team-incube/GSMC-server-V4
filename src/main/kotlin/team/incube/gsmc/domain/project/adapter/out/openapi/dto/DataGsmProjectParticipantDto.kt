package team.incube.gsmc.domain.project.adapter.out.openapi.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import team.incube.gsmc.domain.project.DataGsmProjectParticipant

/**
 * DataGSM 프로젝트 참여자 응답 DTO입니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DataGsmProjectParticipantDto(
    val id: Long,
    val name: String,
    val email: String,
    val studentNumber: Long? = null,
    val major: String? = null,
    val sex: String? = null,
)

/**
 * [DataGsmProjectParticipantDto]를 도메인 모델 [DataGsmProjectParticipant]로 변환한다.
 */
fun DataGsmProjectParticipantDto.toDomain(): DataGsmProjectParticipant =
    DataGsmProjectParticipant(
        participantId = id,
        participantName = name,
        participantEmail = email,
        studentNumber = studentNumber?.toString(),
        major = major,
        sex = sex,
    )
