package team.incube.gsmc.domain.sheet.service

import team.incube.gsmc.domain.score.ScoreAggregator
import team.incube.gsmc.domain.sheet.ScoreSheetRow
import team.incube.gsmc.domain.sheet.SheetStudent
import team.incube.gsmc.domain.sheet.port.`in`.FetchClassScoreSheetUseCase
import team.incube.gsmc.domain.sheet.port.`in`.FetchGradeScoreSheetUseCase
import team.incube.gsmc.domain.sheet.port.out.SheetGeneratorPort
import team.incube.gsmc.domain.sheet.port.out.SheetMemberPersistencePort
import team.incube.gsmc.domain.sheet.port.out.SheetScorePersistencePort
import team.incube.gsmc.domain.sheet.port.out.SheetStoragePort
import team.incube.gsmc.domain.user.UserRole
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.port.Port
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import team.incube.gsmc.global.util.MemberUtil
import java.util.UUID

private const val XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

/**
 * 학급·학년별 점수 현황 파일 조회 유스케이스 구현 클래스입니다.
 * [FetchClassScoreSheetUseCase]와 [FetchGradeScoreSheetUseCase]를 구현하며, ROOT 역할만 호출할 수 있습니다.
 * 학생과 승인 점수를 조회해 총점을 계산하고, XLSX 파일을 생성·저장한 뒤 임시 다운로드 URL을 반환합니다.
 */
@Port(direction = PortDirection.INBOUND)
class FetchScoreSheetService(
    private val sheetMemberPersistencePort: SheetMemberPersistencePort,
    private val sheetScorePersistencePort: SheetScorePersistencePort,
    private val sheetGeneratorPort: SheetGeneratorPort,
    private val sheetStoragePort: SheetStoragePort,
    private val memberUtil: MemberUtil,
) : FetchClassScoreSheetUseCase,
    FetchGradeScoreSheetUseCase {
    /** 지정한 학급의 점수 현황 파일을 생성하고 다운로드 URL을 반환합니다. */
    override fun execute(
        grade: Int,
        classNumber: Int,
    ): String {
        requireRoot()
        validateGrade(grade)
        validateClassNumber(classNumber)

        return generateSheet(
            students = sheetMemberPersistencePort.findAllStudentsByGradeAndClass(grade, classNumber),
            sheetName = "${grade}학년 ${classNumber}반",
            keyPrefix = "sheets/class/$grade/$classNumber",
        )
    }

    /** 지정한 학년 전체의 점수 현황 파일을 생성하고 다운로드 URL을 반환합니다. */
    override fun execute(grade: Int): String {
        requireRoot()
        validateGrade(grade)

        return generateSheet(
            students = sheetMemberPersistencePort.findAllStudentsByGrade(grade),
            sheetName = "${grade}학년",
            keyPrefix = "sheets/grade/$grade",
        )
    }

    private fun generateSheet(
        students: List<SheetStudent>,
        sheetName: String,
        keyPrefix: String,
    ): String {
        val eligibleStudents =
            students
                .filter { it.role == UserRole.STUDENT }
                .sortedWith(compareBy(SheetStudent::grade, SheetStudent::classNumber, SheetStudent::number))
        val totalScoreByUserId =
            sheetScorePersistencePort.findApprovedScoresByUserIds(eligibleStudents.map { it.userId })
        val rows =
            eligibleStudents.map { student ->
                ScoreSheetRow(
                    grade = student.grade,
                    classNumber = student.classNumber,
                    number = student.number,
                    name = student.name,
                    totalScore =
                        ScoreAggregator.totalScoreOf(
                            totalScoreByUserId[student.userId].orEmpty(),
                            includeApprovedOnly = true,
                            userGrade = student.grade,
                        ),
                )
            }
        val content = sheetGeneratorPort.generate(rows, sheetName)
        val key = "$keyPrefix/${UUID.randomUUID()}.xlsx"

        sheetStoragePort.upload(key, content, XLSX_CONTENT_TYPE)
        return sheetStoragePort.createPresignedDownloadUrl(key)
    }

    private fun requireRoot() {
        if (memberUtil.getCurrentUserRole() != UserRole.ROOT) {
            throw GsmcException(ErrorCode.FORBIDDEN)
        }
    }

    private fun validateGrade(grade: Int) {
        if (grade !in 1..3) {
            throw GsmcException(ErrorCode.INVALID_GRADE)
        }
    }

    private fun validateClassNumber(classNumber: Int) {
        if (classNumber !in 1..4) {
            throw GsmcException(ErrorCode.INVALID_CLASS_NUMBER)
        }
    }
}
