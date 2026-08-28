@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.sheet.port.`in`

/** 학년별 점수 현황 파일 다운로드 URL 조회 유스케이스입니다. */
interface FetchGradeScoreSheetUseCase {
    /**
     * 지정한 학년의 점수 현황 파일을 생성하고 다운로드 URL을 반환합니다.
     *
     * @param grade 대상 학년
     * @return 생성된 점수 현황 파일의 다운로드 URL
     */
    fun execute(grade: Int): String
}
