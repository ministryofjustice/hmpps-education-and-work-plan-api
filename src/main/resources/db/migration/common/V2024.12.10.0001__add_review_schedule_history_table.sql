--- Add the table review schedule history

--- review_schedule table
CREATE TABLE review_schedule_history
(
    id                         UUID PRIMARY KEY,
    reference                  UUID                        NOT NULL,
    version                    INT                         NOT NULL,
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

CREATE INDEX idx_review_schedule_history_prison_number
    ON review_schedule_history (prison_number);
