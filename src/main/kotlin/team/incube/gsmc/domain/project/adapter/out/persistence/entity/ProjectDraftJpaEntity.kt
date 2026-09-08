package team.incube.gsmc.domain.project.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/** 사용자별 단일 프로젝트 초안과 참여자·파일 연결을 저장하는 JPA 엔티티입니다. */
@Entity
@Table(
    name = "project_draft_tb",
    uniqueConstraints = [UniqueConstraint(name = "uk_project_draft_user", columnNames = ["user_id"])],
)
class ProjectDraftJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_draft_id", nullable = false)
    val projectDraftId: Long = 0,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserJpaEntity,
    @Column(name = "draft_title", nullable = false, length = 255)
    val title: String,
    @Column(name = "draft_description", nullable = false, columnDefinition = "TEXT")
    val description: String,
    @ManyToMany
    @JoinTable(
        name = "project_draft_participant_tb",
        joinColumns = [JoinColumn(name = "project_draft_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["project_draft_id", "user_id"])],
    )
    val participants: MutableSet<UserJpaEntity> = linkedSetOf(),
    @ManyToMany
    @JoinTable(
        name = "project_draft_file_tb",
        joinColumns = [JoinColumn(name = "project_draft_id")],
        inverseJoinColumns = [JoinColumn(name = "file_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["project_draft_id", "file_id"])],
    )
    val files: MutableSet<FileJpaEntity> = linkedSetOf(),
)
