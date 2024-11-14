--- review table indexes
DROP INDEX review_prison_number_idx;
CREATE INDEX review_prison_number_idx ON review
(
    prison_number
);
CREATE UNIQUE INDEX review_reference_idx ON review
(
    reference
);

--- review_schedule table indexes
DROP INDEX review_schedule_prison_number_idx;
CREATE INDEX review_schedule_prison_number_idx ON review_schedule
(
    prison_number
);
CREATE UNIQUE INDEX review_schedule_reference_idx ON review_schedule
(
    reference
);

-- add review schedule reference column to review table
ALTER TABLE review ADD COLUMN review_schedule_reference UUID NOT NULL;
