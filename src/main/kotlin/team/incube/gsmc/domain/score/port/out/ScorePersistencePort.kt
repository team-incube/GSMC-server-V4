package team.incube.gsmc.domain.score.port.out

import team.incube.gsmc.domain.score.Score

/**
 * 점수 요청 영속성을 추상화하는 아웃바운드 포트 인터페이스입니다.
 * category/evidence/file은 항상 함께 조회되어 [Score] 도메인 객체에 채워진다.
 */
interface ScorePersistencePort {
    /**
     * ID로 점수 요청을 조회한다.
     *
     * @param scoreId 조회할 점수 요청 ID
     * @return 해당 점수 요청, 없으면 null
     */
    fun findById(scoreId: Long): Score?

    /**
     * 특정 사용자의 모든 점수 요청을 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 점수 요청 목록
     */
    fun findAllByUserId(userId: Long): List<Score>

    /**
     * 여러 사용자의 모든 점수 요청을 한 번에 조회한다. 백분위 계산처럼 여러 사용자의 점수를 동시에
     * 필요로 하는 경우 사용한다.
     *
     * @param userIds 조회할 사용자 ID 목록
     * @return 해당 사용자들의 점수 요청 목록
     */
    fun findAllByUserIdIn(userIds: List<Long>): List<Score>
}
