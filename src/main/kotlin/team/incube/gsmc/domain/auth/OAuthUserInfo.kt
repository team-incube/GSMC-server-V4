package team.incube.gsmc.domain.auth

/**
 * OAuth 공급자로부터 조회한 사용자 정보
 *
 * DataGSM OAuth를 통해 받아온 학교 구성원 정보를 담는다.
 * 학생 여부에 따라 학년·반·번호 필드가 채워진다.
 *
 * @param email 학교 이메일 (gsm.hs.kr 도메인)
 * @param isStudent 학생 여부 (false이면 교사)
 * @param name 이름 (학생인 경우에만 존재)
 * @param grade 학년 (학생인 경우에만 존재)
 * @param classNum 반 번호 (학생인 경우에만 존재)
 * @param number 번호 (학생인 경우에만 존재)
 */
data class OAuthUserInfo(
    val email: String,
    val isStudent: Boolean,
    val name: String?,
    val grade: Int?,
    val classNum: Int?,
    val number: Int?,
)
