@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Score

/**
 * 값 없는 점수 추가 유스케이스 인터페이스입니다. 증빙이 필요 없고 집계 방식이 COUNT_BASED인
 * 카테고리에 대해 카테고리 유형만으로 본인 명의로 점수를 신청한다(신청 자체가 곧 항목 개수 1건).
 */
interface AppendMyScoreOnlyUseCase {
    /**
     * @param categoryType 신청할 카테고리 유형
     * @return 생성 또는 재사용된 점수 요청
     */
    fun execute(categoryType: CategoryType): Score
}
