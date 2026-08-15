@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

import team.incube.gsmc.domain.file.File

/**
 * 내 파일 목록 조회 유스케이스 인터페이스입니다.
 */
interface FetchMyFilesUseCase {
    /**
     * 현재 로그인한 사용자가 업로드한 모든 파일을 조회한다.
     *
     * @return 파일 목록
     */
    fun execute(): List<File>
}
