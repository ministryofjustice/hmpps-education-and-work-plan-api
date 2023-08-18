ALTER TABLE action_plan ADD COLUMN reference UUID;
ALTER TABLE action_plan ADD COLUMN review_date DATE;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

UPDATE action_plan SET reference = uuid_generate_v4() WHERE reference IS NULL;

ALTER TABLE action_plan ALTER COLUMN reference SET NOT NULL;
