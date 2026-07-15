package team.incube.gsmc.domain.category

/**
 * 역량 평가 카테고리 유형
 *
 * | 값                    | 한글명          | 집계 방식    | 증빙 방식  |
 * |-----------------------|-----------------|--------------|------------|
 * | CERTIFICATE           | 자격증          | COUNT_BASED  | FILE       |
 * | TOPCIT                | TOPCIT          | SCORE_BASED  | FILE       |
 * | TOEIC                 | TOEIC           | SCORE_BASED  | FILE       |
 * | JLPT                  | JLPT            | SCORE_BASED  | FILE       |
 * | TOEIC_ACADEMY         | 토익사관학교    | COUNT_BASED  | UNREQUIRED |
 * | READ_A_THON           | 독서마라톤      | SCORE_BASED  | FILE       |
 * | VOLUNTEER             | 봉사활동        | SCORE_BASED  | UNREQUIRED |
 * | PROJECT_PARTICIPATION | 프로젝트 참여   | COUNT_BASED  | EVIDENCE   |
 * | AWARD                 | 수상경력        | COUNT_BASED  | FILE       |
 * | ACADEMIC_GRADE        | 교과성적        | SCORE_BASED  | UNREQUIRED |
 * | EXTERNAL_ACTIVITY     | 외부활동        | COUNT_BASED  | FILE       |
 * | NCS                   | 직업기초능력평가 | SCORE_BASED | FILE       |
 * | NEWRROW_SCHOOL        | 뉴로우스쿨참여  | SCORE_BASED  | FILE       |
 *
 * `TOEIC_ACADEMY`/`VOLUNTEER`/`READ_A_THON`의 증빙 방식은 점수 추가 API 설계(`docs/plans/score-add-api-plan.md`)에서
 * 재정의되었다 — 실제 `category_tb`의 `evidence_type` 값도 이에 맞게 갱신되어야 한다.
 *
 * `JLPT`는 별도 `category_tb` 행을 갖지 않고 `TOEIC`과 같은 행을 공유한다 — 제출 시 카테고리 조회가
 * TOEIC으로 캐노니컬 매핑되며([team.incube.gsmc.domain.score.service.AppendScoreSupport]), 인정점수도 TOEIC과
 * 동일한 캡 안에서 계산된다. `TOEIC_ACADEMY`는 참여 승인 시 TOEIC 인정점수에 가산되는 보너스로만
 * 반영되며 총점에 독립적으로 집계되지 않는다([team.incube.gsmc.domain.score.ScoreCalculator]).
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

    /** 직업기초능력평가 — 평균 등급 반올림, 최대 5점 */
    NCS,

    /** 뉴로우스쿨참여 — 참여 성실도 회고온도 20점당 1점, 최대 5점 */
    NEWRROW_SCHOOL,
}
