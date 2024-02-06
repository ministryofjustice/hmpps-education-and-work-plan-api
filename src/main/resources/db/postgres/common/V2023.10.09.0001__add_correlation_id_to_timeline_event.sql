ALTER TABLE timeline_event ADD COLUMN correlation_id UUID;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
UPDATE timeline_event SET correlation_id = uuid_generate_v4() WHERE correlation_id IS NULL;

ALTER TABLE timeline_event ALTER COLUMN correlation_id SET NOT NULL;
