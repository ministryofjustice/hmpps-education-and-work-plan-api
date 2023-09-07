ALTER TABLE goal ADD COLUMN created_at_prison VARCHAR(20);
ALTER TABLE goal ADD COLUMN updated_at_prison VARCHAR(20);

UPDATE goal SET created_at_prison = 'MDI';
UPDATE goal SET updated_at_prison = 'MDI';

ALTER TABLE goal ALTER COLUMN created_at_prison SET NOT NULL;
ALTER TABLE goal ALTER COLUMN updated_at_prison SET NOT NULL;
