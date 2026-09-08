---
name: database-schema
description: 이 프로젝트의 DB 스키마 규칙 — xxxx_tb 단수 테이블명, 컬럼 네이밍, 인덱스 전략, Flyway 마이그레이션, JPA Entity 매핑 패턴. 테이블/컬럼 추가나 인덱스 설계 시 참조.
---

# Database Schema 가이드

MySQL + Flyway(`V{n}__{description}.sql`, `src/main/resources/db/migration/`) + JPA(`ddl-auto: validate`) 조합을 사용한다.
즉 **스키마 변경은 반드시 Flyway 마이그레이션 파일로 하고, JPA는 검증만 한다.**

## 네이밍 규칙 ([[architecture]] 참고)

- 테이블: `snake_case`, **단수**, `xxxx_tb` 접미사 — `alert_tb`, `score_tb`, `user_tb`
- PK 컬럼: `{table}_id` (단순 `id`가 아님) — `alert_id`, `score_id`
- 일반 컬럼: `snake_case` — `created_at`, `is_read`
- FK 제약 이름: `fk_{table}_{ref}` — `fk_alert_user`, `fk_alert_score`
- 인덱스 이름: `idx_{table}_{col1}_{col2}` — `idx_alert_user_id_created_at`

## 표준 컬럼

```sql
CREATE TABLE example_tb (
    example_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ...
    created_at DATETIME NOT NULL
);
```
`updated_at`은 필요한 엔티티에만 추가한다 (모든 테이블에 강제하지 않음 — 기존 마이그레이션 참고).

## 실제 예시 — `V4__create_alert_table.sql`

```sql
CREATE TABLE alert_tb (
    alert_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    score_id      BIGINT      NULL,
    alert_type    VARCHAR(20) NOT NULL,
    alert_content TEXT        NOT NULL,
    is_read       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    DATETIME    NOT NULL,
    CONSTRAINT fk_alert_user  FOREIGN KEY (user_id)  REFERENCES user_tb (user_id),
    CONSTRAINT fk_alert_score FOREIGN KEY (score_id) REFERENCES score_tb (score_id)
);

CREATE INDEX idx_alert_user_id_created_at ON alert_tb (user_id, created_at);
```

## 인덱스 전략

- WHERE에 자주 쓰이는 단일 컬럼: 단순 인덱스
- WHERE + ORDER BY 조합: 복합 인덱스 (WHERE 컬럼을 앞에)
- FK 컬럼은 조회 패턴이 있으면 인덱스 추가를 검토 (자동 생성되지 않음)
- `is_read`, `status` 같은 저카디널리티 컬럼은 단독 인덱스 효과가 적음 — 복합 인덱스의 뒤쪽에 배치

## Flyway 마이그레이션

- 파일명: `V{다음 버전}__{설명}.sql` (기존 최댓값은 `find src/main/resources/db/migration -name "V*.sql"`로 확인)
- 이미 적용된 버전 파일은 절대 수정하지 않는다 — 체크섬이 깨져 배포가 실패한다. 변경이 필요하면 새 버전 파일을 추가한다.
- `application.yaml`: `flyway.baseline-on-migrate: true`, `baseline-version: 1` — 기존 DB에 베이스라인을 잡고 시작하는 설정이므로 로컬 초기화 시 유의.

## JPA Entity 매핑 (`adapter/out/persistence/entity/`)

```kotlin
@Entity
@Table(name = "alert_tb")
class AlertJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    val alertId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "alert_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val alertType: AlertType,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
)
```

- 고유 식별자(unique key) 컬럼은 Kotlin 타입도 non-nullable로 맞추고 `unique = true, nullable = false`를 명시한다.
- Enum은 반드시 `@Enumerated(EnumType.STRING)` — `ORDINAL`은 컬럼 순서 변경 시 데이터가 깨진다.
- Entity ↔ Domain 변환은 확장 함수로 (`{Domain}JpaEntity.toDomain()`, `{Domain}.toEntity()`) — [[architecture]] 참고.

## 참고 파일 탐색

```bash
find src/main/resources/db/migration -name "V*.sql" | sort -V | tail -5
find src/main -name "*JpaEntity.kt" | head -5
```
