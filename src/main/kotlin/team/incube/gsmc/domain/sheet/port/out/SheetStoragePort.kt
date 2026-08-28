package team.incube.gsmc.domain.sheet.port.out

/** 생성된 점수 현황 파일을 외부 저장소에 저장하고 조회하는 출력 포트입니다. */
interface SheetStoragePort {
    /** 파일 내용을 지정한 키로 저장합니다. */
    fun upload(
        key: String,
        content: ByteArray,
        contentType: String,
    )

    /** 저장된 파일의 다운로드 URL을 생성합니다. */
    fun createPresignedDownloadUrl(key: String): String
}
