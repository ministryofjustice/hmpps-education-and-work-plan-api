ALTER TABLE induction_schedule ADD COLUMN created_at_prison VARCHAR(3);
ALTER TABLE induction_schedule ADD COLUMN updated_at_prison VARCHAR(3);
ALTER TABLE induction_schedule_history ADD COLUMN created_at_prison VARCHAR(3);
ALTER TABLE induction_schedule_history ADD COLUMN updated_at_prison VARCHAR(3);

UPDATE induction_schedule SET created_at_prison = 'BXI';
UPDATE induction_schedule SET updated_at_prison = 'BXI';

ALTER TABLE induction_schedule ALTER COLUMN created_at_prison SET NOT NULL;
ALTER TABLE induction_schedule ALTER COLUMN updated_at_prison SET NOT NULL;

UPDATE induction_schedule_history SET created_at_prison = 'BXI';
UPDATE induction_schedule_history SET updated_at_prison = 'BXI';

ALTER TABLE induction_schedule_history ALTER COLUMN created_at_prison SET NOT NULL;
ALTER TABLE induction_schedule_history ALTER COLUMN updated_at_prison SET NOT NULL;