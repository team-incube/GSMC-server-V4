@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.score.port.`in`

import team.incube.gsmc.domain.category.CategoryType
import team.incube.gsmc.domain.score.Score

/**
 * 증빙자료(텍스트) 기반 점수 추가 유스케이스 인터페이스입니다. 증빙 방식이 EVIDENCE인 카테고리에
 * 대해 활동 내용을 텍스트로 입력해 본인 명의로 점수를 신청한다. `PROJECT_PARTICIPATION`은 전용
 * 플로우(DataGSM 연동)로만 신청 가능해 이 유스케이스의 대상에서 제외된다.
 */
interface AppendMyScoreWithEvidenceUseCase {
    /**
     * @param categoryType 신청할 카테고리 유형
     * @param value 활동명으로 저장될 문자열, 없을 수 있음
     * @return 생성 또는 재사용된 점수 요청
     */
    fun execute(
        categoryType: CategoryType,
        value: String?,
    ): Score
}
