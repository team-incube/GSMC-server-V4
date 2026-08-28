package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.sheet.SheetStudent

/** 점수 현황 파일에 필요한 학생 정보를 조회하는 출력 포트입니다. */
interface SheetMemberPersistencePort {
    /** 지정한 학년의 학생을 조회합니다. */
    fun findAllStudentsByGrade(grade: Int): List<SheetStudent>

    /** 지정한 학년과 반의 학생을 조회합니다. */
    fun findAllStudentsByGradeAndClass(
        grade: Int,
        classNumber: Int,
    ): List<SheetStudent>
}
