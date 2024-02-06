--- Make optional columns nullable
ALTER TABLE timeline_event ALTER COLUMN contextual_info DROP NOT NULL;
ALTER TABLE timeline_event ALTER COLUMN actioned_by_display_name DROP NOT NULL;
