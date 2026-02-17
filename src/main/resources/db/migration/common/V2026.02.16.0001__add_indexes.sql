CREATE INDEX IF NOT EXISTS idx_note_entity_reference_type
    ON note (entity_reference, entity_type);

CREATE INDEX IF NOT EXISTS idx_rsh_reference_version_desc
    ON review_schedule_history (reference, version DESC);

CREATE INDEX IF NOT EXISTS idx_ih_reference_version_desc
    ON induction_schedule_history (reference, version DESC);

CREATE INDEX IF NOT EXISTS idx_timeline_event_timeline_created
    ON timeline_event (timeline_id, created_at);



