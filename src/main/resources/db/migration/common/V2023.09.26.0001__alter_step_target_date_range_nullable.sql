--- Alter step table to make target_date_range nullable
ALTER TABLE step
    ALTER COLUMN target_date_range DROP NOT NULL;
