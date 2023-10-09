ALTER TABLE timeline_event ADD COLUMN correlation_id UUID;

UPDATE timeline_event SET correlation_id = random_uuid() WHERE correlation_id IS NULL;

ALTER TABLE timeline_event ALTER COLUMN correlation_id SET NOT NULL;
