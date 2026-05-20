package team.incube.gsmc.domain.category

/**
 * 카테고리별 증빙 자료 제출 방식
 *
 * | 값         | 제출 방식            | 예시                        |
 * |------------|---------------------|-----------------------------|
 * | EVIDENCE   | 텍스트/URL 입력     | 수상경력 (대회명, 수상 내역) |
 * | FILE       | 파일 업로드         | 자격증 사본 PDF/이미지       |
 * | UNREQUIRED | 증빙 불필요         | 교과성적 (시스템 자동 연동)  |
 */
enum class EvidenceType {
    /** 텍스트 또는 URL로 증빙 */
    EVIDENCE,

    /** 파일 업로드로 증빙 */
    FILE,

    /** 증빙 자료 불필요 */
    UNREQUIRED,
}
