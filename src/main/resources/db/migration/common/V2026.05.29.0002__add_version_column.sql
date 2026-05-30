--- Add version columns to skills_and_interests table

ALTER TABLE skills_and_interests
    ADD COLUMN version INTEGER;
UPDATE skills_and_interests
    SET version = 1;
ALTER TABLE skills_and_interests
    ALTER COLUMN version SET NOT NULL;
