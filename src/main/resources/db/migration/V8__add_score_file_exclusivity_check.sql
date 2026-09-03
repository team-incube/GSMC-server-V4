-- score_tb: 출처(evidence / project / dg_project)는 최대 하나만 채워질 수 있다.
-- 셋 다 null인 경우는 증빙이 필요 없는 카테고리의 정상 상태이므로 허용한다.
ALTER TABLE score_tb
    ADD CONSTRAINT chk_score_source CHECK (
        (evidence_id IS NOT NULL) + (project_id IS NOT NULL) + (dg_project_id IS NOT NULL) <= 1
        );

-- file_tb: 한 파일은 score 또는 evidence 중 한쪽에만 연결될 수 있다.
-- 둘 다 null인 경우는 업로드 확인만 되고 아직 연결되지 않은 정상 상태이므로 허용한다.
ALTER TABLE file_tb
    ADD CONSTRAINT chk_file_link CHECK (
        score_id IS NULL OR evidence_id IS NULL
        );
