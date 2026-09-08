package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.sheet.SheetStudent

interface SheetMemberPersistencePort {
    fun findAllStudentsByGrade(grade: Int): List<SheetStudent>

    fun findAllStudentsByGradeAndClass(
        grade: Int,
        classNumber: Int,
    ): List<SheetStudent>
}
