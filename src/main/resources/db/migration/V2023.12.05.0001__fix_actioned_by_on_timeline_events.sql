-- Update the actioned_by field on timeline events where it has been set incorrectly previously.
UPDATE timeline_event
    SET actioned_by = (
        SELECT g.updated_by
        FROM timeline_event te
            INNER JOIN goal g ON g.reference::text = te.source_reference
        WHERE te.reference = timeline_event.reference
    )
    WHERE EXISTS (
        SELECT *
        FROM timeline_event te
            INNER JOIN goal g ON g.reference::text = te.source_reference
        WHERE (te.event_type = 'GOAL_UPDATED' OR te.event_type = 'INDUCTION_UPDATED')
          AND te.actioned_by != g.updated_by
    );
