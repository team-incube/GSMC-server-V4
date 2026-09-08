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
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import team.incube.gsmc.domain.file.adapter.out.persistence.entity.FileJpaEntity
import team.incube.gsmc.domain.user.adapter.out.persistence.entity.UserJpaEntity

/** 내부 프로젝트와 참여자·파일 연결을 저장하는 JPA 엔티티입니다. */
@Entity
@Table(name = "project_tb")
class ProjectJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", nullable = false)
    val projectId: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: UserJpaEntity,
    @Column(name = "project_title", nullable = false, length = 100)
    val title: String,
    @Column(name = "project_description", nullable = false, length = 2000)
    val description: String,
    @ManyToMany
    @JoinTable(
        name = "project_participant_tb",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "user_id"])],
    )
    val participants: MutableSet<UserJpaEntity> = linkedSetOf(),
    @ManyToMany
    @JoinTable(
        name = "project_file_tb",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "file_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "file_id"])],
    )
    val files: MutableSet<FileJpaEntity> = linkedSetOf(),
)
