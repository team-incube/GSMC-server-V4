package team.incube.gsmc.domain.sheet.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.sheet.SheetStudent
import team.incube.gsmc.domain.sheet.port.out.SheetMemberPersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.QUserJpaEntity.userJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

@Adapter(direction = PortDirection.OUTBOUND)
class SheetMemberPersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
) : SheetMemberPersistencePort {
    override fun findAllStudentsByGrade(grade: Int): List<SheetStudent> =
        findStudents(grade = grade, classNumber = null)

    override fun findAllStudentsByGradeAndClass(
        grade: Int,
        classNumber: Int,
    ): List<SheetStudent> = findStudents(grade, classNumber)

    private fun findStudents(
        grade: Int,
        classNumber: Int?,
    ): List<SheetStudent> =
        queryFactory
            .selectFrom(userJpaEntity)
            .where(
                userJpaEntity.userRole.eq(UserRole.STUDENT),
                userJpaEntity.userGrade.eq(grade),
                classNumber?.let { userJpaEntity.userClassNumber.eq(it) },
                userJpaEntity.userClassNumber.isNotNull,
                userJpaEntity.userNumber.isNotNull,
            ).orderBy(
                userJpaEntity.userGrade.asc(),
                userJpaEntity.userClassNumber.asc(),
                userJpaEntity.userNumber.asc(),
            ).fetch()
            .map {
                SheetStudent(
                    userId = it.userId,
                    grade = it.userGrade!!,
                    classNumber = it.userClassNumber!!,
                    number = it.userNumber!!,
                    name = it.userName,
                    role = it.userRole,
                )
            }
}
