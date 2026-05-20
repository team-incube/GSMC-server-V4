package team.incube.gsmc.domain.category

/**
 * 역량 평가 카테고리 유형
 *
 * | 값                    | 한글명        | 집계 방식    | 증빙 방식  |
 * |-----------------------|---------------|--------------|------------|
 * | CERTIFICATE           | 자격증        | COUNT_BASED  | FILE       |
 * | TOPCIT                | TOPCIT        | SCORE_BASED  | FILE       |
 * | TOEIC                 | TOEIC         | SCORE_BASED  | FILE       |
 * | JLPT                  | JLPT          | SCORE_BASED  | FILE       |
 * | TOEIC_ACADEMY         | 토익사관학교  | COUNT_BASED  | EVIDENCE   |
 * | READ_A_THON           | 독서마라톤    | SCORE_BASED  | EVIDENCE   |
 * | VOLUNTEER             | 봉사활동      | SCORE_BASED  | FILE       |
 * | PROJECT_PARTICIPATION | 프로젝트 참여 | COUNT_BASED  | EVIDENCE   |
 * | AWARD                 | 수상경력      | COUNT_BASED  | EVIDENCE   |
 * | ACADEMIC_GRADE        | 교과성적      | SCORE_BASED  | UNREQUIRED |
 * | EXTERNAL_ACTIVITY     | 외부활동      | COUNT_BASED  | EVIDENCE   |
 */
enum class CategoryType {
    CERTIFICATE,
    TOPCIT,
    TOEIC,
    JLPT,
    TOEIC_ACADEMY,
    READ_A_THON,
    VOLUNTEER,
    PROJECT_PARTICIPATION,
    AWARD,
    ACADEMIC_GRADE,
    EXTERNAL_ACTIVITY,
}
