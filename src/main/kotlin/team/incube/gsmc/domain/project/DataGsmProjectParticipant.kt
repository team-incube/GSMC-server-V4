package team.incube.gsmc.domain.project

/**
 * DataGSM 프로젝트 참여자 도메인 모델(조회 전용 값 객체)
 *
 * @param participantId DataGSM 학생 고유 식별자
 * @param participantName 참여자 이름
 * @param participantEmail 참여자 학교 이메일
 * @param studentNumber 학번
 * @param major 전공
 * @param sex 성별
 */
data class DataGsmProjectParticipant(
    val participantId: Long,
    val participantName: String,
    val participantEmail: String,
    val studentNumber: String?,
    val major: String?,
    val sex: String?,
)
