--- Add version columns to future_work_interests table

ALTER TABLE future_work_interests
    ADD COLUMN version INTEGER;
UPDATE future_work_interests
    SET version = 1;
ALTER TABLE future_work_interests
    ALTER COLUMN version SET NOT NULL;

ALTER TABLE previous_work_experiences
    ADD COLUMN version INTEGER;
UPDATE previous_work_experiences
SET version = 1;
ALTER TABLE previous_work_experiences
    ALTER COLUMN version SET NOT NULL;
