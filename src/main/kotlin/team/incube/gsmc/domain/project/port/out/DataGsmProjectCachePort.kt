package team.incube.gsmc.domain.project.port.out

import team.incube.gsmc.domain.project.DataGsmProject

/** DataGSM 전체 ACTIVE 프로젝트 목록 캐시를 추상화하는 포트이다. */
interface DataGsmProjectCachePort {
    /** 캐시된 전체 ACTIVE 프로젝트 목록을 조회한다. 캐시가 없으면 null을 반환한다. */
    fun findAll(): List<DataGsmProject>?

    /** 전체 ACTIVE 프로젝트 목록을 캐시에 저장한다. */
    fun saveAll(projects: List<DataGsmProject>)
}
