ALTER TABLE action_plan ADD COLUMN reference UUID;
ALTER TABLE action_plan ADD COLUMN review_date_category VARCHAR(50);
ALTER TABLE action_plan ADD COLUMN review_date DATE;

UPDATE action_plan SET reference = random_uuid(), review_date_category = 'NO_DATE' WHERE reference IS NULL;

ALTER TABLE action_plan ALTER COLUMN reference SET NOT NULL;
ALTER TABLE action_plan ALTER COLUMN review_date_category SET NOT NULL;
