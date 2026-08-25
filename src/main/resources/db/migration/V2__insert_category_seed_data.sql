ALTER TABLE category_tb
    ADD COLUMN conversion_divisor INT NOT NULL DEFAULT 1;

INSERT INTO category_tb
    (category_english_name, category_korean_name, category_type, evidence_type, calculation_type, weight, category_maximum_value, conversion_divisor, is_accumulated)
VALUES
    ('Certificate',           '자격증',          'CERTIFICATE',           'FILE',       'COUNT_BASED', 2, 7,  1,   TRUE),
    ('Topcit',                'TOPCIT',          'TOPCIT',                'FILE',       'SCORE_BASED', 1, 10, 100, FALSE),
    ('Toeic',                 'TOEIC',           'TOEIC',                 'FILE',       'SCORE_BASED', 1, 10, 100, FALSE),
    ('Toeic Academy',         '토익사관학교',     'TOEIC_ACADEMY',         'UNREQUIRED', 'COUNT_BASED', 1, 1,  1,   FALSE),
    ('Read A Thon',           '독서마라톤',       'READ_A_THON',           'FILE',       'SCORE_BASED', 1, 7,  1,   FALSE),
    ('Volunteer',             '봉사활동',        'VOLUNTEER',             'UNREQUIRED', 'SCORE_BASED', 1, 10, 1,   TRUE),
    ('Project Participation', '프로젝트 참여',    'PROJECT_PARTICIPATION', 'EVIDENCE',   'COUNT_BASED', 2, 5,  1,   TRUE),
    ('Award',                 '수상경력',        'AWARD',                 'FILE',       'COUNT_BASED', 1, 10, 1,   TRUE),
    ('Academic Grade',        '교과성적',        'ACADEMIC_GRADE',        'UNREQUIRED', 'SCORE_BASED', 1, 9,  1,   FALSE),
    ('External Activity',     '외부활동',        'EXTERNAL_ACTIVITY',     'FILE',       'COUNT_BASED', 1, 10, 1,   TRUE),
    ('NCS',                   '직업기초능력평가', 'NCS',                   'FILE',       'SCORE_BASED', 1, 5,  1,   FALSE),
    ('Newrrow School',        '뉴로우스쿨참여',   'NEWRROW_SCHOOL',        'FILE',       'SCORE_BASED', 1, 5,  20,  FALSE);
