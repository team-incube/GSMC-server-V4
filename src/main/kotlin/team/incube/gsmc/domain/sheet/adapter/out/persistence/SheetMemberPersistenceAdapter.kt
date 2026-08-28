package team.incube.gsmc.domain.sheet.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import team.incube.gsmc.domain.sheet.SheetStudent
import team.incube.gsmc.domain.sheet.port.out.SheetMemberPersistencePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.QUserJpaEntity.userJpaEntity
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter

/** 점수 현황 파일에 필요한 학생 정보를 조회하는 영속성 어댑터입니다. */
@Adapter(direction = PortDirection.OUTBOUND)
class SheetMemberPersistenceAdapter(
    private val queryFactory: JPAQueryFactory,
) : SheetMemberPersistencePort {
    /** 지정한 학년의 학생을 반과 번호 순서로 조회합니다. */
    override fun findAllStudentsByGrade(grade: Int): List<SheetStudent> =
        findStudents(grade = grade, classNumber = null)

    /** 지정한 학년과 반의 학생을 번호 순서로 조회합니다. */
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
