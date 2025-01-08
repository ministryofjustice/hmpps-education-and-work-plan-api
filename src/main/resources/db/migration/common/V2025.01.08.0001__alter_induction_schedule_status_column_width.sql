ALTER TABLE induction_schedule
    ALTER COLUMN schedule_status TYPE VARCHAR(60);

ALTER TABLE induction_schedule_history
    ALTER COLUMN schedule_status TYPE VARCHAR(60);
