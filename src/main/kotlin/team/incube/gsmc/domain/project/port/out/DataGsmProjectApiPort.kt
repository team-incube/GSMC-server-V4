package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.DataGsmProject

/**
 * DataGSM 프로젝트 데이터 OpenAPI 연동을 추상화하는 아웃바운드 포트 인터페이스입니다.
 */
interface DataGsmProjectApiPort {
    /**
     * 참여자 이메일이 포함된 ACTIVE 프로젝트 목록을 조회합니다.
     *
     * @param email 조회할 참여자 이메일
     * @return 해당 이메일이 참여자로 포함된 ACTIVE 프로젝트 목록
     */
    fun findActiveProjectsByParticipantEmail(email: String): List<DataGsmProject>

    /**
     * ID로 프로젝트를 조회합니다.
     *
     * @param dgProjectId 조회할 DataGSM 프로젝트 ID
     * @return 해당 프로젝트, 없으면 null
     */
    fun findProjectById(dgProjectId: Long): DataGsmProject?
}
