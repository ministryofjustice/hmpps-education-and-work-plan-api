DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'employability_skill_rating'
    ) THEN

CREATE TABLE employability_skill_rating
(
    code        TEXT PRIMARY KEY,
    description TEXT NOT NULL,
    score       INTEGER NOT NULL
);

INSERT INTO employability_skill_rating (code, description, score) VALUES
              ('NOT_CONFIDENT',     'not confident',      1),
              ('LITTLE_CONFIDENT',  'a little confident', 2),
              ('QUITE_CONFIDENT',   'quite confident',    3),
              ('VERY_CONFIDENT',    'very confident',     4);

CREATE TABLE employability_skill
(
    id                         UUID PRIMARY KEY,
    reference                  UUID        NOT NULL,
    prison_number              VARCHAR(10) NOT NULL,
    skill_type                 VARCHAR(255) NOT NULL,
    evidence                   TEXT        NOT NULL,
    rating_code                TEXT        NOT NULL,
    activity_name              TEXT        NOT NULL,
    conversation_date          DATE,
    created_at                 TIMESTAMP   NOT NULL,
    created_by                 VARCHAR(50) NOT NULL,
    created_at_prison          VARCHAR(3)  NOT NULL,
    updated_at                 TIMESTAMP   NOT NULL,
    updated_by                 VARCHAR(50) NOT NULL,
    updated_at_prison          VARCHAR(3)  NOT NULL,

    CONSTRAINT fk_employability_skill_rating
        FOREIGN KEY (rating_code)
            REFERENCES employability_skill_rating(code),

    CONSTRAINT chk_employability_skill_type
        CHECK (skill_type IN (
                              'TEAMWORK',
                              'TIMEKEEPING',
                              'COMMUNICATION',
                              'PLANNING',
                              'ORGANISATION',
                              'PROBLEM_SOLVING',
                              'INITIATIVE',
                              'ADAPTABILITY',
                              'RELIABILITY',
                              'CREATIVITY'
            ))
);

CREATE INDEX idx_employability_skill_prison_number
    ON employability_skill (prison_number);

CREATE INDEX idx_employability_skill_rating_code
    ON employability_skill (rating_code);

CREATE INDEX idx_employability_skill_skill_type
    ON employability_skill (skill_type);

END IF;
END $$;
