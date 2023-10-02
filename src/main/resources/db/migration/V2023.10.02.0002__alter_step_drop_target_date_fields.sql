--- Alter step table to drop target date fields
ALTER TABLE step
    DROP COLUMN target_date;
ALTER TABLE step
    DROP COLUMN target_date_range;
