package team.incube.gsmc.domain.evidence.port.out

import team.incube.gsmc.domain.evidence.Evidence

/**
 * 근거 자료 영속성을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface EvidencePersistencePort {
    /**
     * ID로 근거 자료를 조회한다.
     *
     * @param evidenceId 조회할 근거 자료 ID
     * @return 해당 근거 자료, 없으면 null
     */
    fun findById(evidenceId: Long): Evidence?

    /**
     * 근거 자료를 저장한다. [evidence]의 evidenceId가 기존 근거 자료의 ID와 같으면 값을 갈아끼우고(update),
     * 0이면 새로 생성한다(insert).
     *
     * @param evidence 저장할 근거 자료 도메인 객체
     * @return 저장된 근거 자료 도메인 객체
     */
    fun save(evidence: Evidence): Evidence
}
