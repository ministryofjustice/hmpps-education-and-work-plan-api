--- Add created_at_prison and updated_at_prison fields to qualification
--- and update existing records with the value from the parent previous_qualifications table

ALTER TABLE qualification ADD COLUMN created_at_prison VARCHAR(3);
ALTER TABLE qualification ADD COLUMN updated_at_prison VARCHAR(3);

UPDATE qualification q
SET
    created_at_prison = (
        SELECT pq.created_at_prison
        FROM previous_qualifications pq
        WHERE q.prev_qualifications_id = pq.id
    ),
    updated_at_prison = (
        SELECT pq.updated_at_prison
        FROM previous_qualifications pq
        WHERE q.prev_qualifications_id = pq.id
    )
;

ALTER TABLE qualification ALTER COLUMN created_at_prison SET NOT NULL;
ALTER TABLE qualification ALTER COLUMN updated_at_prison SET NOT NULL;
