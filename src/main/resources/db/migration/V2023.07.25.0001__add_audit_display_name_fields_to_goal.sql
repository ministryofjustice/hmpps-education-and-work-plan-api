ALTER TABLE goal ADD COLUMN created_by_display_name VARCHAR(100);
ALTER TABLE goal ADD COLUMN updated_by_display_name VARCHAR(100);

UPDATE goal SET created_by_display_name = created_by;
UPDATE goal SET updated_by_display_name = updated_by;

ALTER TABLE goal ALTER COLUMN created_by_display_name VARCHAR(100) NOT NULL;
ALTER TABLE goal ALTER COLUMN updated_by_display_name VARCHAR(100) NOT NULL;
