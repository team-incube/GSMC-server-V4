ALTER TABLE file_tb
    ADD CONSTRAINT uk_file_file_uri UNIQUE (file_uri);
