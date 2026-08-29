package team.incube.gsmc.domain.project.adapter.web

/** Project 초안 저장 GraphQL 입력입니다. */
data class CreateProjectDraftInput(
    /** 저장할 초안 제목입니다. */
    val title: String = "",
    /** 저장할 초안 설명입니다. */
    val description: String = "",
    /** 초안에 연결할 파일 식별자 목록입니다. */
    val fileIds: List<Long> = emptyList(),
    /** 초안에 등록할 참여자 식별자 목록입니다. */
    val participantIds: List<Long> = emptyList(),
)
