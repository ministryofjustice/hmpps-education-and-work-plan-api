ALTER TABLE induction_schedule ADD COLUMN created_at_prison VARCHAR(3) DEFAULT 'BXI';
ALTER TABLE induction_schedule ADD COLUMN updated_at_prison VARCHAR(3) DEFAULT 'BXI';
ALTER TABLE induction_schedule_history ADD COLUMN created_at_prison VARCHAR(3) DEFAULT 'BXI';
ALTER TABLE induction_schedule_history ADD COLUMN updated_at_prison VARCHAR(3) DEFAULT 'BXI';