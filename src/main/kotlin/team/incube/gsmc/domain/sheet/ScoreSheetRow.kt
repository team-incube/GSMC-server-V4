package team.incube.gsmc.domain.sheet

/** 점수 현황 엑셀 파일의 한 행을 표현하는 모델입니다. */
data class ScoreSheetRow(
    /** 학생의 학년입니다. */
    val grade: Int,
    /** 학생의 반 번호입니다. */
    val classNumber: Int,
    /** 학생의 번호입니다. */
    val number: Int,
    /** 학생의 이름입니다. */
    val name: String,
    /** 승인된 점수를 기준으로 계산한 총점입니다. */
    val totalScore: Int,
)
