--- Add the exemption_reason column to induction schedule

ALTER TABLE induction_schedule
ADD COLUMN exemption_reason VARCHAR(512);

--- induction_schedule table
CREATE TABLE induction_schedule_history (
    id                          UUID                            PRIMARY KEY,
    reference                   UUID                            NOT NULL,
    prison_number               VARCHAR(10)                     NOT NULL,
    deadline_date               DATE                            NOT NULL,
    schedule_calculation_rule   VARCHAR(60)                     NOT NULL,
    schedule_status             VARCHAR(50)                     NOT NULL,
    created_at                  TIMESTAMP(3) WITH TIME ZONE     NOT NULL,
    created_by                  VARCHAR(50)                     NOT NULL,
    updated_at                  TIMESTAMP(3) WITH TIME ZONE     NOT NULL,
    updated_by                  VARCHAR(50)                     NOT NULL,
    exemption_reason            VARCHAR(512)                            ,
    version                     INT                             NOT NULL
);

CREATE INDEX idx_induction_schedule_history_prison_number
    ON induction_schedule_history (prison_number);


