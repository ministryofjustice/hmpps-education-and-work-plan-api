ALTER TABLE action_plan ADD COLUMN reference UUID;
ALTER TABLE action_plan ADD COLUMN review_date DATE;

UPDATE action_plan SET reference = random_uuid() WHERE reference IS NULL;
ALTER TABLE action_plan ALTER COLUMN reference SET NOT NULL;
