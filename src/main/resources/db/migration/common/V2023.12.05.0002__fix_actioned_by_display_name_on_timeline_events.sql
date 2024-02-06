-- Update the actioned_by_display_name field on timeline events where it has been set incorrectly previously.
UPDATE timeline_event
SET actioned_by_display_name = (
    SELECT g.updated_by_display_name
    FROM timeline_event te
      INNER JOIN goal g ON g.reference::text = te.source_reference
    WHERE te.reference = timeline_event.reference
)
WHERE id IN (
    SELECT te.id
    FROM timeline_event te
      INNER JOIN goal g ON g.reference::text = te.source_reference
    WHERE te.event_type = 'GOAL_UPDATED'
      AND te.actioned_by_display_name != g.updated_by_display_name
);

UPDATE timeline_event
SET actioned_by_display_name = (
    SELECT i.updated_by_display_name
    FROM timeline_event te
             INNER JOIN induction i ON i.reference::text = te.source_reference
    WHERE te.reference = timeline_event.reference
)
WHERE id IN (
    SELECT te.id
    FROM timeline_event te
             INNER JOIN induction i ON i.reference::text = te.source_reference
    WHERE te.event_type = 'INDUCTION_UPDATED'
      AND te.actioned_by_display_name != i.updated_by_display_name
);

UPDATE timeline_event
SET actioned_by = (
    SELECT i.updated_by
    FROM timeline_event te
             INNER JOIN induction i ON i.reference::text = te.source_reference
    WHERE te.reference = timeline_event.reference
)
WHERE id IN (
    SELECT te.id
    FROM timeline_event te
             INNER JOIN induction i ON i.reference::text = te.source_reference
    WHERE te.event_type = 'INDUCTION_UPDATED'
      AND te.actioned_by != i.updated_by
);
