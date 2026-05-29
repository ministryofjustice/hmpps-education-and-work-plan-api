--- Add version columns to in_prison_interests table

ALTER TABLE in_prison_interests
    ADD COLUMN version INTEGER;
UPDATE in_prison_interests
    SET version = 1;
ALTER TABLE in_prison_interests
    ALTER COLUMN version SET NOT NULL;
