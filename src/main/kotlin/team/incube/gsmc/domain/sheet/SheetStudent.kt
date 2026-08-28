package team.incube.gsmc.domain.sheet

import team.incube.gsmc.domain.user.UserRole

/** 점수 현황 엑셀 파일 생성을 위해 조회한 학생 정보입니다. */
data class SheetStudent(
    /** 학생 회원의 식별자입니다. */
    val userId: Long,
    /** 학생의 학년입니다. */
    val grade: Int,
    /** 학생의 반 번호입니다. */
    val classNumber: Int,
    /** 학생의 번호입니다. */
    val number: Int,
    /** 학생의 이름입니다. */
    val name: String,
    /** 회원의 역할입니다. */
    val role: UserRole,
)
