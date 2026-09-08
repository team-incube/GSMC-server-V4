---
name: migration-guide
description: DB 스키마 변경과 Entity 수정의 영향 분석 및 올바른 변경 순서(헥사고날 계층 + Flyway) — 컬럼 삭제 시 2단계 배포 전략 포함. 스키마 변경을 계획할 때 사용.
---

# DB / Entity 변경 가이드

이 프로젝트는 `ddl-auto: validate`이므로 **스키마는 Flyway 마이그레이션으로만 바뀐다.** JPA Entity를 먼저 고치고 마이그레이션을 잊으면 애플리케이션이 기동 시점에 검증 실패로 죽는다.

## 변경 전 체크리스트

- [ ] 기존 데이터에 미치는 영향을 분석했는가? (NOT NULL 컬럼 추가 시 기본값/백필 필요)
- [ ] 롤백 전략을 세웠는가? (Flyway는 자동 롤백을 지원하지 않으므로, 실패 시 새 마이그레이션으로 되돌린다)
- [ ] 인덱스가 필요한 대용량 테이블이면 잠금 시간을 고려했는가?

## 변경 순서 (헥사고날 아키텍처 기준)

1. **Flyway 마이그레이션 작성**: `src/main/resources/db/migration/V{n}__{description}.sql` — [[database-schema]]의 네이밍 규칙 준수
2. **domain/ 수정**: 순수 Kotlin 클래스, 새 필드 추가/제거
3. **adapter/out/persistence/entity/ 수정**: `{Domain}JpaEntity`에 컬럼 매핑 추가, `toDomain()`/`toEntity()` 확장 함수 갱신
4. **port/out, port/in DTO 수정**: 영향받는 UseCase 시그니처, 요청/응답 DTO
5. **service/ 수정**: 비즈니스 로직 반영
6. **adapter/in (GraphQL 스키마) 수정**: `*.graphqls`에 필드 추가, non-null 여부 결정
7. **테스트 갱신**: Entity/Service 테스트 ([[kotest-guide]] 참고)

> 순서를 지키는 이유: `domain/`이 인프라에 의존하지 않는 계층이므로, 도메인 모델을 먼저 확정한 뒤 바깥쪽(영속성 → 서비스 → 어댑터)으로 전파해야 컴파일 에러로 누락을 조기에 발견할 수 있다.

## 컬럼 삭제 — 2단계 배포

운영 중인 컬럼을 바로 지우면 배포 순서에 따라 애플리케이션이 존재하지 않는 컬럼을 참조해 에러가 난다.

1. **1단계 (Deprecate)**: 코드에서 해당 컬럼 참조를 전부 제거 (Entity 필드, 쿼리, DTO). 컬럼 자체는 DB에 유지.
2. **배포 후 검증**: 운영에서 문제없이 동작하는지 확인.
3. **2단계 (Delete)**: 별도 마이그레이션(`ALTER TABLE ... DROP COLUMN ...`)으로 실제 컬럼 삭제.

## NOT NULL 컬럼 추가

기존 행이 있는 테이블에 NOT NULL 컬럼을 추가할 때:

```sql
-- 1) DEFAULT와 함께 추가 (기존 행 자동 백필)
ALTER TABLE score_tb ADD COLUMN dg_project_id BIGINT NOT NULL DEFAULT 0;

-- 2) 필요하면 후속 마이그레이션에서 DEFAULT 제약을 제거
```

## 참고 파일 탐색

```bash
find src/main/resources/db/migration -name "V*.sql" | sort -V | tail -1   # 최신 버전 번호 확인
find src/main -type d -name "domain" -path "*/main/*"
```
