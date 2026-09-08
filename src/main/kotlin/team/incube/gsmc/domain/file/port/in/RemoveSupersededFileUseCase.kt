@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.file.port.`in`

import team.incube.gsmc.domain.file.File

/**
 * 밀려난 점수에 딸린 파일을 정리하는 내부 전용 유스케이스 인터페이스입니다.
 *
 * 비누적 카테고리에서 더 높은 점수가 승인되면 기존 승인 점수 행이 삭제되는데, 그때 거기 연결돼
 * 있던 증빙 파일을 함께 정리합니다. [RemoveFileUseCase]와 달리 소유자 검증과
 * "승인된 점수에 연결된 파일은 삭제 불가" 가드를 거치지 않습니다. 점수 행 자체가 사라져 감사
 * 추적 대상이 없어지는 상황이라 그 가드의 전제가 성립하지 않기 때문입니다.
 *
 * 사용자가 직접 호출하는 경로가 아니므로 GraphQL 스키마에 노출하지 않습니다. 호출자는
 * [team.incube.gsmc.domain.score.service.ApproveScoreService] 뿐입니다.
 */
interface RemoveSupersededFileUseCase {
    /**
     * @param file 삭제할 파일
     */
    fun execute(file: File)
}
