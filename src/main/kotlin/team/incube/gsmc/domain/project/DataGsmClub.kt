package team.incube.gsmc.domain.project

/**
 * DataGSM 동아리 도메인 모델(프로젝트 소유 동아리 정보를 표현하기 위한 조회 전용 값 객체)
 *
 * @param clubId 동아리 고유 식별자
 * @param clubName 동아리명
 * @param clubType 동아리 유형
 */
data class DataGsmClub(
    val clubId: Long,
    val clubName: String,
    val clubType: String?,
)
