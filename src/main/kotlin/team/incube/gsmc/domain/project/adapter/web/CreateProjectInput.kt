package team.incube.gsmc.domain.project.adapter.web

/** Project 생성 GraphQL 입력입니다. */
data class CreateProjectInput(
    /** 생성할 프로젝트 제목입니다. */
    val title: String,
    /** 생성할 프로젝트 설명입니다. */
    val description: String,
    /** 연결할 파일 식별자 목록입니다. */
    val fileIds: List<Long> = emptyList(),
    /** 참여자로 등록할 사용자 식별자 목록입니다. */
    val participantIds: List<Long>,
)
