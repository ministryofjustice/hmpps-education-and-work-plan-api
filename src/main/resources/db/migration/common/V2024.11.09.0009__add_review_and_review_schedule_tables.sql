--- Add the tables for reviews and review schedules

--- review table
CREATE TABLE review
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    prison_number              VARCHAR(10)                 NOT NULL,
    deadline_date              DATE                        NOT NULL,
    completed_date             DATE                        NOT NULL,
    conducted_by               VARCHAR(200),
    conducted_by_role          VARCHAR(200),
    created_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by                 VARCHAR(50)                 NOT NULL,
    created_at_prison          VARCHAR(3)                  NOT NULL,
    updated_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by                 VARCHAR(50)                 NOT NULL,
    updated_at_prison          VARCHAR(3)                  NOT NULL
);
CREATE UNIQUE INDEX review_prison_number_idx ON review
(
    prison_number
);

--- review_schedule table
CREATE TABLE review_schedule
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    prison_number              VARCHAR(10)                 NOT NULL,
    earliest_review_date       DATE                        NOT NULL,
    latest_review_date         DATE                        NOT NULL,
    schedule_calculation_rule  VARCHAR(50)                 NOT NULL,
    schedule_status            VARCHAR(50)                 NOT NULL,
    created_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_by                 VARCHAR(50)                 NOT NULL,
    created_at_prison          VARCHAR(3)                  NOT NULL,
    updated_at                 TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by                 VARCHAR(50)                 NOT NULL,
    updated_at_prison          VARCHAR(3)                  NOT NULL
);
CREATE UNIQUE INDEX review_schedule_prison_number_idx ON review
(
    prison_number
);
