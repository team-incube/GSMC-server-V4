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
