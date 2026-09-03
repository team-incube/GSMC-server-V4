-- 도메인 Score.file과 GraphQL Score.file은 단일 값인데 DB에는 한 점수에 여러 파일이 붙는 것을
-- 막는 제약이 없어, 조회 시 fetchFirst()로 임의의 1건만 집히고 나머지는 유실된 것처럼 동작했다.
-- MySQL의 UNIQUE는 NULL을 여러 개 허용하므로 nullable 컬럼에 그대로 걸 수 있다
-- (연결되지 않은 파일은 score_id가 NULL이라 제약 대상이 아니다).
ALTER TABLE file_tb
    ADD CONSTRAINT uk_file_score UNIQUE (score_id);
