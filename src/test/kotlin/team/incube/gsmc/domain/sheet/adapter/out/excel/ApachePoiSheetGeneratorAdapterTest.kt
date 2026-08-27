package team.incube.gsmc.domain.sheet.adapter.out.excel

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import team.incube.gsmc.domain.sheet.ScoreSheetRow
import java.io.ByteArrayInputStream

class ApachePoiSheetGeneratorAdapterTest :
    BehaviorSpec({
        val adapter = ApachePoiSheetGeneratorAdapter()

        Given("점수 행이 있으면") {
            When("XLSX를 생성하면") {
                Then("헤더, 정렬된 행, 셀 타입과 값을 보존한다") {
                    val content =
                        adapter.generate(
                            listOf(
                                ScoreSheetRow(2, 3, 1, "=이름", 0),
                                ScoreSheetRow(2, 3, 2, "홍길동", 15),
                            ),
                            "2학년 3반",
                        )

                    XSSFWorkbook(ByteArrayInputStream(content)).use { workbook ->
                        val sheet = workbook.getSheet("2학년 3반")
                        sheet.lastRowNum shouldBe 2
                        sheet.getRow(0).map { it.stringCellValue }.shouldContainExactly("학년", "반", "번호", "이름", "총점")
                        sheet.getRow(1).getCell(0).cellType shouldBe CellType.NUMERIC
                        sheet.getRow(1).getCell(3).cellType shouldBe CellType.STRING
                        sheet.getRow(1).getCell(3).stringCellValue shouldBe "=이름"
                        sheet.getRow(1).getCell(4).numericCellValue shouldBe 0.0
                        sheet.getRow(2).getCell(4).numericCellValue shouldBe 15.0
                    }
                }
            }
        }

        Given("학생이 없으면") {
            When("XLSX를 생성하면") {
                Then("헤더만 있는 유효한 파일을 생성한다") {
                    val content = adapter.generate(emptyList(), "3학년")

                    XSSFWorkbook(ByteArrayInputStream(content)).use { workbook ->
                        val sheet = workbook.getSheet("3학년")
                        sheet.lastRowNum shouldBe 0
                        sheet.getRow(0).physicalNumberOfCells shouldBe 5
                    }
                }
            }
        }
    })
