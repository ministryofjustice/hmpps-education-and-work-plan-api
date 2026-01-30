--- RR-2239 / production incident INC4080827

--- Flyway script to revert all Completed Goals for A5740AQ in production from Complete back to Active by:
---   deleting the goal completion note
---   deleting the timeline record
---   changing the step status to active
---   changing the goal status to active

--- Delete the Goal Completion Note associated with the Goal
delete from note
where note.id in (
    select n.id from note n
    inner join goal g on n.entity_reference = g.reference
    inner join action_plan ap on ap.id = g.action_plan_id
    where ap.prison_number = 'A5740AQ'
    and g.status = 'COMPLETED'
    and n.note_type = 'GOAL_COMPLETION'
);

--- Delete the timeline record associated with the Goal completion
delete from timeline_event_contextual_info
where timeline_event_contextual_info.timeline_event_id in (
    select te.id from timeline_event te
    inner join timeline t on t.id = te.timeline_id
    inner join goal g on te.source_reference = g.reference::varchar
    inner join action_plan ap on ap.id = g.action_plan_id
    where ap.prison_number = 'A5740AQ'
    and g.status = 'COMPLETED'
    and te.event_type = 'GOAL_COMPLETED'
);
delete from timeline_event
where timeline_event.id in (
    select te.id from timeline_event te
    inner join timeline t on t.id = te.timeline_id
    inner join goal g on te.source_reference = g.reference::varchar
    inner join action_plan ap on ap.id = g.action_plan_id
    where ap.prison_number = 'A5740AQ'
    and g.status = 'COMPLETED'
    and te.event_type = 'GOAL_COMPLETED'
);

--- Change the Goal's Step status to ACTIVE and update the updatedXXX audit fields
update step
set
    status = 'ACTIVE',
    updated_by = created_by,
    updated_at = created_at
where step.id in (
    select s.id from step s
    inner join goal g on s.goal_id = g.id
    inner join action_plan ap on ap.id = g.action_plan_id
    where ap.prison_number = 'A5740AQ'
    and g.status = 'COMPLETED'
    and s.status = 'COMPLETE'
);

--- Update the Goal status to ACTIVE and update the updatedXXX audit fields
update goal
set
    status = 'ACTIVE',
    updated_by = created_by,
    updated_at = created_at,
    updated_at_prison = created_at_prison
where goal.id in (
    select g.id from goal g
    inner join action_plan ap on ap.id = g.action_plan_id
    where ap.prison_number = 'A5740AQ'
    and g.status = 'COMPLETED'
);