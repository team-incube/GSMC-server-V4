package team.incube.gsmc.domain.sheet.port.out

import team.incube.gsmc.domain.sheet.ScoreSheetRow

/** 점수 현황 행 목록을 파일 형식으로 변환하는 출력 포트입니다. */
interface SheetGeneratorPort {
    /**
     * 점수 현황 행 목록을 파일 내용으로 변환합니다.
     *
     * @param rows 파일에 기록할 학생별 점수 현황
     * @param sheetName 파일 내 시트 이름
     * @return 생성된 파일의 바이트 배열
     */
    fun generate(
        rows: List<ScoreSheetRow>,
        sheetName: String,
    ): ByteArray
}
