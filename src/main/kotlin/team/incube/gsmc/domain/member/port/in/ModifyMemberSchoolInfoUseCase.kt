@file:Suppress("ktlint:standard:package-name")

package team.incube.gsmc.domain.member.port.`in`

/**
 * 회원 학적정보(학년/반/번호) 변경 유스케이스 인터페이스입니다.
 */
interface ModifyMemberSchoolInfoUseCase {
    /**
     * 대상 회원의 학적정보를 변경한다.
     *
     * @param memberId 학적정보를 변경할 회원 ID
     * @param grade 변경할 학년, 학적정보를 비우려면 null
     * @param classNumber 변경할 반 번호, 학적정보를 비우려면 null
     * @param number 변경할 번호, 학적정보를 비우려면 null
     * @return 성공 여부
     * @throws team.incube.gsmc.global.exception.GsmcException 호출자가 ROOT가 아니면 [team.incube.gsmc.global.exception.ErrorCode.FORBIDDEN]
     * @throws team.incube.gsmc.global.exception.GsmcException 대상 회원이 없으면 [team.incube.gsmc.global.exception.ErrorCode.USER_NOT_FOUND]
     * @throws team.incube.gsmc.global.exception.GsmcException 학적정보 조합이 대상 회원의 권한에 맞지 않으면 [team.incube.gsmc.global.exception.ErrorCode.INVALID_SCHOOL_INFO]
     * @throws team.incube.gsmc.global.exception.GsmcException 다른 회원이 이미 같은 학년·반·번호를 사용 중이면 [team.incube.gsmc.global.exception.ErrorCode.DUPLICATE_RESOURCE]
     */
    fun execute(
        memberId: Long,
        grade: Int?,
        classNumber: Int?,
        number: Int?,
    ): Boolean
}
