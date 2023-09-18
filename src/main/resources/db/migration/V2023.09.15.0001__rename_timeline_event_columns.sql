--- Rename created_by/created_at columns in timeline_event

ALTER TABLE timeline_event
    RENAME COLUMN created_at_prison TO prison_id;
ALTER TABLE timeline_event
    RENAME COLUMN created_by TO actioned_by;
ALTER TABLE timeline_event
    RENAME COLUMN created_by_display_name TO actioned_by_display_name;
