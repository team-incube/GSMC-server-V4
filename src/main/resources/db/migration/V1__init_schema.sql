CREATE TABLE user_tb (
    user_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name          VARCHAR(25)  NOT NULL,
    user_email         VARCHAR(100) NOT NULL,
    user_grade         INT          NULL,
    user_class_number  INT          NULL,
    user_number        INT          NULL,
    user_role          VARCHAR(50)  NOT NULL,
    CONSTRAINT uk_user_email UNIQUE (user_email),
    CONSTRAINT uk_user_grade_class_number UNIQUE (user_grade, user_class_number, user_number)
);

CREATE TABLE category_tb (
    category_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    weight                  INT         NOT NULL,
    category_english_name   VARCHAR(50) NOT NULL,
    category_korean_name    VARCHAR(50) NOT NULL,
    category_maximum_value  INT         NOT NULL,
    is_accumulated          BOOLEAN     NOT NULL,
    evidence_type           VARCHAR(20) NOT NULL,
    category_type           VARCHAR(30) NOT NULL,
    calculation_type        VARCHAR(20) NOT NULL
);

CREATE TABLE evidence_tb (
    evidence_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    evidence_title       VARCHAR(255) NOT NULL,
    evidence_content     TEXT         NOT NULL,
    evidence_created_at  DATETIME     NOT NULL,
    evidence_updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_evidence_user FOREIGN KEY (user_id) REFERENCES user_tb (user_id)
);

CREATE TABLE score_tb (
    score_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    category_id       BIGINT       NOT NULL,
    evidence_id       BIGINT       NULL,
    score_status      VARCHAR(20)  NOT NULL,
    activity_name     VARCHAR(255) NULL,
    score_value       INT          NULL,
    rejection_reason  VARCHAR(500) NULL,
    dg_project_id     BIGINT       NULL,
    created_at        DATETIME     NOT NULL,
    updated_at        DATETIME     NOT NULL,
    CONSTRAINT fk_score_user     FOREIGN KEY (user_id)     REFERENCES user_tb (user_id),
    CONSTRAINT fk_score_category FOREIGN KEY (category_id) REFERENCES category_tb (category_id),
    CONSTRAINT fk_score_evidence FOREIGN KEY (evidence_id) REFERENCES evidence_tb (evidence_id)
);

CREATE TABLE file_tb (
    file_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    score_id             BIGINT       NULL,
    evidence_id          BIGINT       NULL,
    file_uri             VARCHAR(255) NOT NULL,
    file_original_name   VARCHAR(255) NOT NULL,
    file_stored_name     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_file_user     FOREIGN KEY (user_id)     REFERENCES user_tb (user_id),
    CONSTRAINT fk_file_score    FOREIGN KEY (score_id)    REFERENCES score_tb (score_id),
    CONSTRAINT fk_file_evidence FOREIGN KEY (evidence_id) REFERENCES evidence_tb (evidence_id)
);
