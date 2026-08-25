package team.incube.gsmc.domain.user

/**
 * 사용자 권한 역할
 *
 * | 값               | 대상        | 주요 권한                             |
 * |------------------|-------------|---------------------------------------|
 * | UNAUTHORIZED     | 미인증      | 기능 접근 불가                        |
 * | STUDENT          | 학생        | 점수 요청, 근거 자료 제출             |
 * | TEACHER          | 교사        | 점수 승인/반려                        |
 * | ROOT             | 최고 관리자 | 카테고리 관리, 사용자 역할 변경       |
 * | HOMEROOM_TEACHER | 담임 교사   | 담당 학급 학생 점수 조회 및 승인/반려 |
 */
enum class UserRole {
    /** 회원가입 직후 초기 상태 */
    UNAUTHORIZED,

    /** 학생 */
    STUDENT,

    /** 교사 */
    TEACHER,

    /** 최고 관리자 */
    ROOT,

    /** 담임 교사 */
    HOMEROOM_TEACHER,
}

/** 교사 권한(TEACHER, HOMEROOM_TEACHER, ROOT) 이상인지 여부 */
fun UserRole.isTeacherOrAbove(): Boolean =
    this == UserRole.TEACHER || this == UserRole.HOMEROOM_TEACHER || this == UserRole.ROOT
