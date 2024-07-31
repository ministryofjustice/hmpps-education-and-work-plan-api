--- Associate previous qualifications with the prisoner (disassociating it from the induction) by:
---   1. Add `prison_number` column to `previous_qualifications` allowing nulls
---   2. Populate `previous_qualifications.prison_number` from the associated induction record
---   3. Make the new `prison_number` column non-nullable
---   4. Drop the column `induction.previous_qualifications_id`


--- 1. Add `prison_number` column to `previous_qualifications`
ALTER TABLE previous_qualifications ADD COLUMN prison_number VARCHAR(10);
CREATE UNIQUE INDEX previous_qualifications_prison_number_idx ON previous_qualifications
(
    prison_number
);

--- 2. Populate `previous_qualifications.prison_number` from the associated induction record
UPDATE previous_qualifications pq
SET prison_number = (
    SELECT i.prison_number
    FROM induction i
    WHERE i.previous_qualifications_id = pq.id
);

--- 3. Add non-null constraint to `previous_qualifications.prison_number`
ALTER TABLE previous_qualifications ALTER COLUMN prison_number SET NOT NULL;

--- 4. Drop the column `induction.previous_qualifications_id`
ALTER TABLE induction
DROP COLUMN previous_qualifications_id;
