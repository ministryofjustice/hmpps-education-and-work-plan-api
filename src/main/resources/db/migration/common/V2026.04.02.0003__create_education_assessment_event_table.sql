CREATE TABLE education_assessment_event
(
    id                 UUID         PRIMARY KEY,
    reference          UUID         NOT NULL UNIQUE,
    prison_number      VARCHAR(10)  NOT NULL,
    status             VARCHAR(60)  NOT NULL,
    status_change_date DATE         NOT NULL,
    source             VARCHAR(50)  NOT NULL,
    detail_url         VARCHAR(512),
    created_at         TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    created_at_prison  VARCHAR(3)   NOT NULL,
    created_by         VARCHAR(50)  NOT NULL,
    updated_at         TIMESTAMP(3) WITH TIME ZONE NOT NULL,
    updated_by         VARCHAR(50)  NOT NULL,
    updated_at_prison  VARCHAR(3)   NOT NULL
);

CREATE INDEX idx_education_assessment_event_prison_number
    ON education_assessment_event (prison_number);
