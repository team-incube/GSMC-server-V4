-- 비누적(is_accumulated = FALSE) 카테고리는 사용자당 "승인된 점수 1건 + 승인 대기 중인 점수 1건"까지만
-- 존재할 수 있다. is_accumulated가 category_tb에 있어 score_tb의 CHECK로는 표현할 수 없고
-- (CHECK는 같은 행만 참조 가능), MySQL은 부분 유니크 인덱스도 지원하지 않으므로
-- 제약이 필요한 행만 담는 별도 테이블의 UNIQUE로 표현한다.
--
-- slot_kind는 score_status 4종을 2종으로 접은 값이다. 자리를 둘로 나눠야 재심사 중에도
-- 기존 승인 점수가 유지된다 (승인 행과 심사 중인 행이 공존).
--   APPROVED                          -> 'APPROVED'
--   PENDING / REJECTED / INCOMPLETE   -> 'UNAPPROVED'
CREATE TABLE score_unique_slot_tb (
    score_id    BIGINT      NOT NULL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    category_id BIGINT      NOT NULL,
    slot_kind   VARCHAR(20) NOT NULL,
    CONSTRAINT uk_score_unique_slot UNIQUE (user_id, category_id, slot_kind),
    CONSTRAINT fk_score_unique_slot_score FOREIGN KEY (score_id) REFERENCES score_tb (score_id) ON DELETE CASCADE,
    CONSTRAINT fk_score_unique_slot_user FOREIGN KEY (user_id) REFERENCES user_tb (user_id),
    CONSTRAINT fk_score_unique_slot_category FOREIGN KEY (category_id) REFERENCES category_tb (category_id)
);

-- 기존 비누적 카테고리 점수에 슬롯 부여.
-- (user_id, category_id, slot_kind) 중복이 이미 있으면 이 INSERT가 실패하는데, 이는 정리해야 할
-- 데이터가 있다는 신호이므로 조용히 넘기지 않고 마이그레이션을 실패시킨다.
INSERT INTO score_unique_slot_tb (score_id, user_id, category_id, slot_kind)
SELECT s.score_id,
       s.user_id,
       s.category_id,
       CASE WHEN s.score_status = 'APPROVED' THEN 'APPROVED' ELSE 'UNAPPROVED' END
FROM score_tb s
         JOIN category_tb c ON c.category_id = s.category_id
WHERE c.is_accumulated = FALSE;
