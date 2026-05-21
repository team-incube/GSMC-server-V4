package team.incube.gsmc.domain.user.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.gsmc.domain.user.UserRole

/**
 * 사용자 정보 엔티티
 *
 * 학교 구성원(학생/교사)의 기본 정보를 저장한다.
 * 학년·반·번호 조합으로 학생을 고유하게 식별할 수 있다.
 *
 * @see UserRole
 */
@Entity
@Table(
    name = "user_tb",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_grade_class_number",
            columnNames = ["user_grade", "user_class_number", "user_number"],
        ),
    ],
)
class UserJpaEntity(
    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,
    /** 사용자 이름 */
    @Column(name = "user_name", nullable = false, length = 25)
    val userName: String,
    /** 학교 이메일 */
    @Column(name = "user_email", nullable = false, unique = true, length = 255)
    val userEmail: String,
    /** 학년 (1~3), 교사는 null */
    @Column(name = "user_grade")
    val userGrade: Int?,
    /** 반 번호, 교사는 null */
    @Column(name = "user_class_number")
    val userClassNumber: Int?,
    /** 번호, 교사는 null */
    @Column(name = "user_number")
    val userNumber: Int?,
    /** 권한 역할 — 가입 시 [UserRole.UNAUTHORIZED], 승인 후 역할 변경 */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 50)
    val userRole: UserRole,
)
