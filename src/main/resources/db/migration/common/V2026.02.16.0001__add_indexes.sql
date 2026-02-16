CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_note_entity_reference_type
    ON note (entity_reference, entity_type);

