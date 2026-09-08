CREATE TABLE project_tb (
    project_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id            BIGINT       NOT NULL,
    project_title       VARCHAR(100) NOT NULL,
    project_description VARCHAR(2000) NOT NULL,
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES user_tb (user_id)
);

CREATE TABLE project_participant_tb (
    project_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_participant_project FOREIGN KEY (project_id) REFERENCES project_tb (project_id),
    CONSTRAINT fk_project_participant_user FOREIGN KEY (user_id) REFERENCES user_tb (user_id)
);

CREATE TABLE project_file_tb (
    project_id BIGINT NOT NULL,
    file_id    BIGINT NOT NULL,
    PRIMARY KEY (project_id, file_id),
    CONSTRAINT fk_project_file_project FOREIGN KEY (project_id) REFERENCES project_tb (project_id),
    CONSTRAINT fk_project_file_file FOREIGN KEY (file_id) REFERENCES file_tb (file_id)
);

CREATE TABLE project_draft_tb (
    project_draft_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    draft_title       VARCHAR(255) NOT NULL,
    draft_description TEXT         NOT NULL,
    CONSTRAINT uk_project_draft_user UNIQUE (user_id),
    CONSTRAINT fk_project_draft_user FOREIGN KEY (user_id) REFERENCES user_tb (user_id)
);

CREATE TABLE project_draft_participant_tb (
    project_draft_id BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    PRIMARY KEY (project_draft_id, user_id),
    CONSTRAINT fk_project_draft_participant_draft FOREIGN KEY (project_draft_id) REFERENCES project_draft_tb (project_draft_id),
    CONSTRAINT fk_project_draft_participant_user FOREIGN KEY (user_id) REFERENCES user_tb (user_id)
);

CREATE TABLE project_draft_file_tb (
    project_draft_id BIGINT NOT NULL,
    file_id          BIGINT NOT NULL,
    PRIMARY KEY (project_draft_id, file_id),
    CONSTRAINT fk_project_draft_file_draft FOREIGN KEY (project_draft_id) REFERENCES project_draft_tb (project_draft_id),
    CONSTRAINT fk_project_draft_file_file FOREIGN KEY (file_id) REFERENCES file_tb (file_id)
);

ALTER TABLE score_tb
    ADD COLUMN project_id BIGINT NULL,
    ADD CONSTRAINT fk_score_project FOREIGN KEY (project_id) REFERENCES project_tb (project_id);

CREATE INDEX idx_project_title ON project_tb (project_title);
CREATE INDEX idx_project_participant_user_id ON project_participant_tb (user_id);
CREATE INDEX idx_score_project_id ON score_tb (project_id);
