package team.incube.gsmc.domain.sheet.adapter.out.excel

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import team.incube.gsmc.domain.sheet.ScoreSheetRow
import team.incube.gsmc.domain.sheet.port.out.SheetGeneratorPort
import team.incube.gsmc.global.annotation.PortDirection
import team.incube.gsmc.global.annotation.adapter.Adapter
import team.incube.gsmc.global.exception.ErrorCode
import team.incube.gsmc.global.exception.GsmcException
import java.io.ByteArrayOutputStream

private val HEADERS = listOf("학년", "반", "번호", "이름", "총점")

@Adapter(direction = PortDirection.OUTBOUND)
class ApachePoiSheetGeneratorAdapter : SheetGeneratorPort {
    override fun generate(
        rows: List<ScoreSheetRow>,
        sheetName: String,
    ): ByteArray =
        try {
            ByteArrayOutputStream().use { output ->
                XSSFWorkbook().use { workbook ->
                    val sheet = workbook.createSheet(sheetName.take(31))
                    val headerStyle =
                        workbook.createCellStyle().apply {
                            alignment = HorizontalAlignment.CENTER
                            verticalAlignment = VerticalAlignment.CENTER
                            setFont(workbook.createFont().apply { bold = true })
                        }
                    val headerRow = sheet.createRow(0)
                    HEADERS.forEachIndexed { index, header ->
                        headerRow.createCell(index).apply {
                            setCellValue(header)
                            cellStyle = headerStyle
                        }
                    }

                    rows.forEachIndexed { rowIndex, row ->
                        val dataRow = sheet.createRow(rowIndex + 1)
                        dataRow.createCell(0).setCellValue(row.grade.toDouble())
                        dataRow.createCell(1).setCellValue(row.classNumber.toDouble())
                        dataRow.createCell(2).setCellValue(row.number.toDouble())
                        dataRow.createCell(3).apply {
                            setCellType(CellType.STRING)
                            setCellValue(row.name)
                        }
                        dataRow.createCell(4).setCellValue(row.totalScore.toDouble())
                    }

                    HEADERS.indices.forEach { index ->
                        sheet.autoSizeColumn(index)
                        sheet.setColumnWidth(index, (sheet.getColumnWidth(index) + 512).coerceAtMost(255 * 256))
                    }
                    workbook.write(output)
                }
                output.toByteArray()
            }
        } catch (_: Exception) {
            throw GsmcException(ErrorCode.SHEET_GENERATION_FAILED)
        }
}
