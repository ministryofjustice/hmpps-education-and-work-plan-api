--- Table goal - make review_date nullable
ALTER TABLE goal ALTER COLUMN review_date DROP NOT NULL;

--- Table step - add target_date_range
ALTER TABLE step ADD COLUMN target_date_range VARCHAR(50);
