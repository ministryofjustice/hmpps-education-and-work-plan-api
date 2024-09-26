CREATE TABLE induction_schedule (
    id                          UUID                            PRIMARY KEY,
    reference                   UUID                            NOT NULL,
    prison_number               VARCHAR(10)                     NOT NULL,
    deadline_date               DATE                            NOT NULL,
    schedule_calculation_rule   VARCHAR(60)                     NOT NULL,
    schedule_status             VARCHAR(50)                     NOT NULL,
    created_at                  TIMESTAMP(3) WITH TIME ZONE     NOT NULL,
    created_by                  VARCHAR(50)                     NOT NULL,
    updated_at                  TIMESTAMP(3) WITH TIME ZONE     NOT NULL,
    updated_by                  VARCHAR(50)                     NOT NULL
);
CREATE UNIQUE INDEX induction_schedule_reference_idx ON induction_schedule
(
    reference
);
CREATE UNIQUE INDEX induction_schedule_prison_number_idx ON induction_schedule
(
    prison_number
);

