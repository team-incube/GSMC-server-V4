package team.incube.gsmc.domain.category

/**
 * 카테고리 점수 집계 방식
 *
 * | 값          | 집계 방식                               | 적용 카테고리                                           |
 * |-------------|----------------------------------------|---------------------------------------------------------|
 * | COUNT_BASED | 항목 개수 (`list.size`)                | 자격증, 토익사관학교, 프로젝트 참여, 수상경력, 외부활동 |
 * | SCORE_BASED | scoreValue 합산 (`sumOf { scoreValue }`) | TOPCIT, TOEIC, JLPT, 독서마라톤, 봉사활동, 교과성적   |
 */
enum class ScoreCalculationType {
    /** 항목 개수 자체가 점수 */
    COUNT_BASED,

    /** scoreValue 합산값이 점수 */
    SCORE_BASED,
}
