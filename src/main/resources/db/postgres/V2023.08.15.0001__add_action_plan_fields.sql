ALTER TABLE action_plan ADD COLUMN reference UUID;
ALTER TABLE action_plan ADD COLUMN review_date_category VARCHAR(50);
ALTER TABLE action_plan ADD COLUMN review_date DATE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

UPDATE action_plan SET reference = uuid_generate_v4(), review_date_category = 'NO_DATE' WHERE reference IS NULL;

ALTER TABLE action_plan ALTER COLUMN reference SET NOT NULL;
ALTER TABLE action_plan ALTER COLUMN review_date_category SET NOT NULL;
