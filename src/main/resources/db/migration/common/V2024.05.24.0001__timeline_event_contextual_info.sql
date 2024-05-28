--- Create table to hold contextual info key/value pairs for timeline events
CREATE TABLE timeline_event_contextual_info
(
    timeline_event_id   UUID         NOT NULL,
    name                VARCHAR(512) NOT NULL,
    value               VARCHAR(512) NOT NULL,

    CONSTRAINT fk_timeline_event FOREIGN KEY (timeline_event_id) REFERENCES timeline_event(id)
);
CREATE UNIQUE INDEX timeline_event_id_name_idx on timeline_event_contextual_info(timeline_event_id, name);

--- Migrate existing contextual info data from timeline_event.
INSERT INTO timeline_event_contextual_info (timeline_event_id, name, value)
    SELECT id, 'GOAL_TITLE', contextual_info
    FROM timeline_event
    WHERE
        timeline_event.event_type LIKE 'GOAL_%'
        AND timeline_event.contextual_info IS NOT NULL;
INSERT INTO timeline_event_contextual_info (timeline_event_id, name, value)
    SELECT id, 'STEP_TITLE', contextual_info
    FROM timeline_event
    WHERE
        timeline_event.event_type LIKE 'STEP_%'
        AND timeline_event.contextual_info IS NOT NULL;

--- Drop the contextual_info column from timeline_event
ALTER TABLE timeline_event DROP COLUMN contextual_info;
